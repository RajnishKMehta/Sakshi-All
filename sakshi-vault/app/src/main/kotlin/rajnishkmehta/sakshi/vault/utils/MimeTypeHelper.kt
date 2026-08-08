package rajnishkmehta.sakshi.vault.utils

import android.webkit.MimeTypeMap

object MimeTypeHelper {
    /**
     * Determines the broad media type based on the MIME type string.
     * Identifies popular audio, video, and image types explicitly.
     */
    fun determineMediaType(mimeType: String?): String {
        if (mimeType.isNullOrBlank()) return "UNKNOWN"

        val lowerMime = mimeType.lowercase()

        // Explicit common Audio types (e.g., m4a, mp3, wav)
        if (lowerMime.startsWith("audio/") ||
            lowerMime.contains("mp3") ||
            lowerMime.contains("wav") ||
            lowerMime.contains("m4a")) {
            return "AUDIO"
        }

        // Explicit common Video types
        if (lowerMime.startsWith("video/") ||
            lowerMime.contains("mp4") ||
            lowerMime.contains("mkv")) {
            return "VIDEO"
        }

        // Explicit common Image types
        if (lowerMime.startsWith("image/") ||
            lowerMime.contains("jpeg") ||
            lowerMime.contains("jpg") ||
            lowerMime.contains("png")) {
            return "PHOTO"
        }

        return "UNKNOWN"
    }

    /**
     * Gets a standard file extension for the given MIME type.
     * Returns null if no extension can be determined instead of a fallback.
     */
    fun getExtensionFromMimeType(mimeType: String?): String? {
        if (mimeType.isNullOrBlank()) return null
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        return if (ext.isNullOrBlank()) null else ext
    }
}
