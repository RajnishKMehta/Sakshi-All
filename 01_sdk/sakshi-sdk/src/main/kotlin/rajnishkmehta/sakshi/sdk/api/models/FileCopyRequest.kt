package rajnishkmehta.sakshi.sdk.api.models

import android.net.Uri

/**
 * Represents a file copy payload request submitted to the Vault application.
 *
 * @property fileId Unique identifier for this file submission. Must contain only lowercase a-z, digits 0-9, underscore, or hyphen, and not exceed 255 characters.
 * @property uri Content Uri or file reference pointing to the source file.
 * @property mediaType Media type of file (e.g. "PHOTO", "VIDEO", "AUDIO", "OTHER").
 * @property fileExtension The file extension for the file (e.g. "jpg", "png"). Must not exceed 50 characters, contain spaces, or start/end with a dot.
 * @property timestampEpochMs Epoch timestamp in milliseconds when the file was captured.
 * @property metadata Optional key-value metadata associated with the file.
 */
public data class FileCopyRequest(
    public val fileId: String,
    public val uri: Uri,
    public val mediaType: String,
    public val fileExtension: String,
    public val timestampEpochMs: Long = System.currentTimeMillis(),
    public val metadata: Map<String, String> = emptyMap()
)
