package rajnishkmehta.sakshi.sdk.api.validation

import java.io.File

/**
 * Validates path components to prevent path traversal and ensure secure file system operations.
 */
public object PathValidator {

    /**
     * Allowed media types.
     */
    private val ALLOWED_MEDIA_TYPES = setOf("PHOTO", "VIDEO", "AUDIO", "OTHER")

    /**
     * Validates that the media type is one of the strictly allowed values.
     */
    public fun validateMediaType(mediaType: String) {
        if (mediaType !in ALLOWED_MEDIA_TYPES) {
            throw IllegalArgumentException("Invalid mediaType: $mediaType. Must be one of: $ALLOWED_MEDIA_TYPES")
        }
    }

    /**
     * Validates the fileId as a safe filename component.
     * Allowed: lowercase a-z, digits 0-9, underscore _, hyphen -
     * Must be between 8 and 128 characters, no path separators or spaces.
     */
    public fun validateFileId(fileId: String) {
        if (fileId.length < 8) {
            throw IllegalArgumentException("fileId must be at least 8 characters long")
        }
        if (fileId.length > 128) {
            throw IllegalArgumentException("fileId exceeds maximum length of 128 characters")
        }
        val fileIdRegex = Regex("^[a-z0-9_-]+$")
        if (!fileIdRegex.matches(fileId)) {
            throw IllegalArgumentException("Invalid fileId: contains invalid characters. Only lowercase a-z, digits 0-9, _, - are allowed")
        }
    }

    /**
     * Validates the file extension.
     * Must be between 1 and 50 characters, not start/end with dot, no path separators/spaces.
     */
    public fun validateFileExtension(extension: String) {
        if (extension.length < 1) {
            throw IllegalArgumentException("fileExtension must be at least 1 character long")
        }
        if (extension.length > 50) {
            throw IllegalArgumentException("fileExtension exceeds maximum length of 50 characters")
        }
        if (extension.startsWith(".") || extension.endsWith(".")) {
            throw IllegalArgumentException("fileExtension cannot start or end with a dot")
        }
        if (extension.contains("/") || extension.contains("\\") || extension.contains("..")) {
            throw IllegalArgumentException("Invalid fileExtension: contains path separators or traversal sequences")
        }
        // Allow alphanumeric, dots (not at start/end), hyphens, underscores
        val extensionRegex = Regex("^[a-zA-Z0-9_\\-]+(\\.[a-zA-Z0-9_\\-]+)*$")
        if (!extensionRegex.matches(extension)) {
            throw IllegalArgumentException("Invalid fileExtension: contains invalid characters")
        }
    }

    /**
     * Validates all components for a path construction.
     */
    public fun validatePathComponents(fileId: String, mediaType: String, fileExtension: String) {
        validateMediaType(mediaType)
        validateFileId(fileId)
        validateFileExtension(fileExtension)
    }

    /**
     * Defense-in-depth: Ensure the constructed canonical path stays within the intended root directory.
     */
    public fun validateDestinationPath(rootDirectory: File, constructedFile: File) {
        val canonicalRoot = rootDirectory.canonicalPath
        val canonicalDest = constructedFile.canonicalPath
        if (!canonicalDest.startsWith(canonicalRoot + File.separator) && canonicalDest != canonicalRoot) {
            throw SecurityException("Path traversal detected: Constructed path escapes root directory")
        }
    }
}
