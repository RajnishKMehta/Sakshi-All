package rajnishkmehta.sakshi.sdk.api.models

import android.net.Uri

/**
 * Represents a photo payload request submitted to the Vault application.
 *
 * @property fileId Unique identifier for this photo submission.
 * @property uri Content Uri or file reference pointing to the source photo.
 * @property mediaType Media type of photo (e.g. "PHOTO", "VIDEO", "AUDIO", "OTHER").
 * @property timestampEpochMs Epoch timestamp in milliseconds when the photo was captured.
 * @property metadata Optional key-value metadata associated with the photo.
 */
public data class PhotoRequest(
    public val fileId: String,
    public val uri: Uri,
    public val mediaType: String,
    public val timestampEpochMs: Long = System.currentTimeMillis(),
    public val metadata: Map<String, String> = emptyMap()
)
