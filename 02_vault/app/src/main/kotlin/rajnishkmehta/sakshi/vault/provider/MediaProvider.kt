package rajnishkmehta.sakshi.vault.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.webkit.MimeTypeMap
import kotlinx.coroutines.runBlocking
import rajnishkmehta.sakshi.vault.AppLog as Log
import rajnishkmehta.sakshi.vault.db.VaultDatabase
import java.io.File
import java.io.FileNotFoundException

class MediaProvider : ContentProvider() {

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
    private var database: VaultDatabase? = null

    companion object {
        private const val MEDIA_URI_CODE = 1
        private const val TAG = "MediaProvider"
    }

    override fun onCreate(): Boolean {
        val authority = "${context?.packageName}.mediaprovider"
        // Pattern: content://<authority>/media/{mediaType}/{fileId}
        uriMatcher.addURI(authority, "media/*/*", MEDIA_URI_CODE)

        context?.let {
            database = VaultDatabase.getDatabase(it)
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        throw SecurityException("Write access is not allowed")
    }

    override fun getType(uri: Uri): String? {
        if (uriMatcher.match(uri) != MEDIA_URI_CODE) {
            return null
        }

        val pathSegments = uri.pathSegments
        if (pathSegments.size < 3) return null

        val fileId = pathSegments[2]

        val db = database ?: return null
        val record = runBlocking { db.mediaRecordDao().getRecord(fileId) } ?: return null

        val extension = record.fileExtension.lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw SecurityException("Write access is not allowed")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw SecurityException("Write access is not allowed")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        throw SecurityException("Write access is not allowed")
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        if (mode != "r") {
            throw SecurityException("Write access is not allowed")
        }

        if (uriMatcher.match(uri) != MEDIA_URI_CODE) {
            throw FileNotFoundException("Unsupported URI: $uri")
        }

        val pathSegments = uri.pathSegments
        if (pathSegments.size < 3) {
            throw FileNotFoundException("Invalid URI structure: $uri")
        }

        val mediaType = pathSegments[1].lowercase()
        val fileId = pathSegments[2]

        if (fileId.contains("/") || fileId.contains("..")) {
            throw SecurityException("Path traversal is not allowed")
        }

        val db = database ?: throw IllegalStateException("Database not initialized")
        val record = runBlocking { db.mediaRecordDao().getRecord(fileId) }
            ?: throw FileNotFoundException("Record not found for fileId: $fileId")

        if (record.mediaType.lowercase() != mediaType) {
            throw FileNotFoundException("Media type mismatch")
        }

        val context = context ?: throw IllegalStateException("Context is null")

        // Use the vault path from the database if available
        val filePath = record.vaultPath ?: run {
            // Fallback to recalculating the path
            val mediaDir = File(context.filesDir, "media/$mediaType")
            val fileName = "${fileId}.${record.fileExtension}"
            File(mediaDir, fileName).absolutePath
        }

        val mediaFile = File(filePath)

        // Path traversal protection
        val canonicalPath = mediaFile.canonicalPath
        val expectedDir = File(context.filesDir, "media/$mediaType").canonicalPath
        if (!canonicalPath.startsWith(expectedDir)) {
            throw SecurityException("Path traversal is not allowed")
        }

        if (!mediaFile.exists() || !mediaFile.isFile) {
            throw FileNotFoundException("Media not found: $uri")
        }

        return ParcelFileDescriptor.open(mediaFile, ParcelFileDescriptor.MODE_READ_ONLY)
    }
}
