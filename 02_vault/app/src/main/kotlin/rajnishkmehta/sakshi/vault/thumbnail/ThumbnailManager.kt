package rajnishkmehta.sakshi.vault.thumbnail

import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.util.Size
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rajnishkmehta.sakshi.vault.AppLog as Log

/**
 * Responsible for generating and storing thumbnails for fully copied media files.
 */
object ThumbnailManager {
    private const val TAG = "ThumbnailManager"
    // Sensible small thumbnail size for Portal/gallery preview
    private val THUMBNAIL_SIZE = Size(320, 320)

    /**
     * Generates a thumbnail for a given source media file and saves it in the private Vault storage.
     * Only processes PHOTO and VIDEO types.
     *
     * @param context Application context
     * @param fileId Unique file identifier
     * @param mediaType The media type ("PHOTO", "VIDEO", etc.)
     * @param fileExtension The original file extension
     * @param sourceFile The fully copied source file in vault storage
     * @return The generated thumbnail File, or null if generation was skipped or failed.
     */
    suspend fun generateAndStoreThumbnail(
        context: Context,
        fileId: String,
        mediaType: String,
        fileExtension: String,
        sourceFile: File
    ): File? = withContext(Dispatchers.IO) {
        if (mediaType != "PHOTO" && mediaType != "VIDEO") {
            Log.d(TAG, "Skipping thumbnail generation for unsupported media type: $mediaType")
            return@withContext null
        }

        if (!sourceFile.exists() || sourceFile.length() == 0L) {
            Log.e(TAG, "Cannot generate thumbnail: Source file does not exist or is empty ($fileId)")
            return@withContext null
        }

        try {
            val thumbnailDir = File(context.filesDir, "media/${mediaType.lowercase()}/thumbnail")
            thumbnailDir.mkdirs()

            val thumbnailFile = File(thumbnailDir, "${fileId}.webp")
            Log.d(TAG, "Generating thumbnail for $fileId ($mediaType) at ${thumbnailFile.absolutePath}")

            val bitmap: Bitmap = if (mediaType == "PHOTO") {
                ThumbnailUtils.createImageThumbnail(sourceFile, THUMBNAIL_SIZE, null)
            } else {
                ThumbnailUtils.createVideoThumbnail(sourceFile, THUMBNAIL_SIZE, null)
            }

            FileOutputStream(thumbnailFile).use { out ->
                @Suppress("DEPRECATION")
                val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    Bitmap.CompressFormat.WEBP
                }
                bitmap.compress(format, 80, out)
            }

            Log.d(TAG, "Successfully generated thumbnail for $fileId")
            return@withContext thumbnailFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate thumbnail for $fileId", e)
            return@withContext null
        }
    }
}
