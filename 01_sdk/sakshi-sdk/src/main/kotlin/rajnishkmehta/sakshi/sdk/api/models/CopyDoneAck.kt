package rajnishkmehta.sakshi.sdk.api.models

import android.net.Uri

/**
 * Acknowledgement response sent by Vault application to client applications (e.g. Camera)
 * when a file copy or video synchronization pass has completed successfully.
 *
 * @property fileId Unique identifier matching the recording file.
 * @property originalUri The original source URI that was provided by the client for copying.
 * @property totalCopiedBytes Total number of bytes copied into Vault storage.
 * @property timestampEpochMs Epoch timestamp in milliseconds when copy completed.
 */
public data class CopyDoneAck(
    public val fileId: String,
    public val originalUri: Uri? = null,
    public val totalCopiedBytes: Long = 0L,
    public val timestampEpochMs: Long = System.currentTimeMillis()
)
