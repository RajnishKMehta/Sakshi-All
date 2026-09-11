package rajnishkmehta.sakshi.vault.utils

object MediaTypeHelper {
    /**
     * Gets a standard file extension for the given media type.
     */
    fun getExtensionFromMediaType(mediaType: String): String? {
        return when (mediaType.uppercase()) {
            "PHOTO" -> "jpg"
            "VIDEO" -> "mp4"
            "AUDIO" -> "m4a"
            else -> null // Includes "OTHER" or unknown types
        }
    }
}
