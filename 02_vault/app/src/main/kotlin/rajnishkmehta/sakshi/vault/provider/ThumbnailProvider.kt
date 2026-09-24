package rajnishkmehta.sakshi.vault.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import rajnishkmehta.sakshi.vault.AppLog as Log
import java.io.File
import java.io.FileNotFoundException

class ThumbnailProvider : ContentProvider() {

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    companion object {
        private const val THUMBNAIL_URI_CODE = 1
        private const val TAG = "ThumbnailProvider"
    }

    override fun onCreate(): Boolean {
        val authority = "${context?.packageName}.thumbnailprovider"
        // Pattern: content://<authority>/media/{mediaType}/thumbnail/{fileId}.webp
        uriMatcher.addURI(authority, "media/*/thumbnail/*", THUMBNAIL_URI_CODE)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null // Not supported
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            THUMBNAIL_URI_CODE -> "image/webp"
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null // Not supported
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0 // Not supported
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0 // Not supported
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        if (mode != "r") {
            throw SecurityException("Write access is not allowed")
        }

        if (uriMatcher.match(uri) != THUMBNAIL_URI_CODE) {
            throw FileNotFoundException("Unsupported URI: $uri")
        }

        val pathSegments = uri.pathSegments
        if (pathSegments.size < 4) {
            throw FileNotFoundException("Invalid URI structure: $uri")
        }

        val mediaType = pathSegments[1].lowercase()
        val filenameWithExt = pathSegments[3]

        if (mediaType != "photo" && mediaType != "video") {
            throw FileNotFoundException("Unsupported media type: $mediaType")
        }

        if (filenameWithExt.contains("/") || filenameWithExt.contains("..")) {
            throw SecurityException("Path traversal is not allowed")
        }

        val context = context ?: throw IllegalStateException("Context is null")
        val thumbnailDir = File(context.filesDir, "media/$mediaType/thumbnail")
        val thumbnailFile = File(thumbnailDir, filenameWithExt)

        val canonicalPath = thumbnailFile.canonicalPath
        if (!canonicalPath.startsWith(thumbnailDir.canonicalPath)) {
            throw SecurityException("Path traversal is not allowed")
        }

        if (!thumbnailFile.exists() || !thumbnailFile.isFile) {
            throw FileNotFoundException("Thumbnail not found: $uri")
        }

        return ParcelFileDescriptor.open(thumbnailFile, ParcelFileDescriptor.MODE_READ_ONLY)
    }
}
