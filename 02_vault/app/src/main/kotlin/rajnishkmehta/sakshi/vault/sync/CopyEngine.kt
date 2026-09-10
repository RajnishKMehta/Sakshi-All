package rajnishkmehta.sakshi.vault.sync

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.delay
import rajnishkmehta.sakshi.vault.db.MediaRecord
import rajnishkmehta.sakshi.vault.db.VaultDatabase
import rajnishkmehta.sakshi.vault.storage.StorageManager
import java.io.IOException
import java.io.InputStream

/**
 * Engine responsible for performing photo and incremental video copy operations.
 * It coordinates reading from source Uris with retry logic, writing to modular storage,
 * and maintaining the synchronization state in the Room database.
 */
class CopyEngine(
    private val context: Context,
    private val database: VaultDatabase,
    private val storageManager: StorageManager
) {

    private val mediaRecordDao = database.mediaRecordDao()

    /**
     * Copies a photo from the source URI to secure vault storage, saves metadata, and updates the database.
     *
     * @param fileId Unique identifier for the photo file.
     * @param sourceUriStr Source URI string of the photo.
     * @param mediaType Type of the media.
     * @return The local vault destination path or URI.
     */
    suspend fun copyPhoto(fileId: String, sourceUriStr: String, mediaType: String): String {
        val uri = Uri.parse(sourceUriStr)
        val inputStream = openInputStreamWithRetry(uri)

        val vaultPath = inputStream.use { stream ->
            storageManager.saveMedia(fileId, stream, mediaType)
        }
        val vaultUri = "file://$vaultPath"

        val now = System.currentTimeMillis()
        val existing = mediaRecordDao.getRecord(fileId)
        val record = MediaRecord(
            fileId = fileId,
            originalUri = sourceUriStr,
            vaultUri = vaultUri,
            mediaType = mediaType,
            completionState = "COMPLETED",
            lastCopiedOffset = java.io.File(vaultPath).length(),
            createdTime = existing?.createdTime ?: now,
            updatedTime = now
        )
        mediaRecordDao.insertRecord(record)
        return vaultUri
    }

    /**
     * Performs a single incremental copy pass for a media file (video/audio).
     * Resumes from the last copied offset stored in the database, appends newly written bytes to vault storage,
     * and updates the offset and state in the database.
     *
     * @param fileId Unique identifier for the media file.
     * @param sourceUriStr Source URI string of the media.
     * @param mediaType Type of the media.
     * @return The number of new bytes copied in this pass.
     */
    suspend fun copyMediaIncremental(fileId: String, sourceUriStr: String, mediaType: String): Long {
        val uri = Uri.parse(sourceUriStr)
        val now = System.currentTimeMillis()

        val existing = mediaRecordDao.getRecord(fileId)
        val lastOffset = existing?.lastCopiedOffset ?: 0L
        val created = existing?.createdTime ?: now

        val inputStream = openInputStreamWithRetry(uri)
        val newlyCopiedBytes = inputStream.use { stream ->
            storageManager.appendMediaBytes(fileId, stream, lastOffset, mediaType)
        }

        val newOffset = lastOffset + newlyCopiedBytes
        val vaultPath = storageManager.getDestinationUri(fileId, mediaType)
        val vaultUri = "file://$vaultPath"

        // Update record in database
        val record = MediaRecord(
            fileId = fileId,
            originalUri = sourceUriStr,
            vaultUri = vaultUri,
            mediaType = mediaType,
            completionState = existing?.completionState ?: "SYNCING",
            lastCopiedOffset = newOffset,
            createdTime = created,
            updatedTime = now
        )
        mediaRecordDao.insertRecord(record)

        return newlyCopiedBytes
    }

    /**
     * Opens an [InputStream] for the given [uri] with retry logic.
     * Retries up to 5 times with exponential backoff if the source file cannot be opened.
     */
    private suspend fun openInputStreamWithRetry(
        uri: Uri,
        maxRetries: Int = 5,
        initialDelayMs: Long = 500L
    ): InputStream {
        var lastException: Exception? = null
        var currentDelay = initialDelayMs

        for (attempt in 1..maxRetries) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    return inputStream
                } else {
                    throw IOException("ContentResolver returned null InputStream for $uri")
                }
            } catch (e: SecurityException) {
                throw IOException("Permission denied for URI: $uri", e)
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries) {
                    delay(currentDelay)
                    currentDelay *= 2 // Exponential backoff
                }
            }
        }
        throw lastException ?: IOException("Failed to open source URI: $uri after $maxRetries attempts")
    }
}
