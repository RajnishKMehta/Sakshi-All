package rajnishkmehta.sakshi.sdk.api.models

/**
 * Result of querying Vault regarding the existence and sync status of a recording.
 *
 * @property fileId Unique identifier of video recording.
 * @property exists True if Vault recognizes and has recorded entry for this file ID.
 * @property state Sync state if recording exists, or `null`.
 * @property lastCopiedOffsetBytes Last recorded byte offset stored by Vault.
 * @property isCompleted True if Vault marks recording as fully copied and finalized.
 */
public data class RecordingQueryResponse(
    public val fileId: String,
    public val exists: Boolean,
    public val state: AVSyncStatus.State? = null,
    public val lastCopiedOffsetBytes: Long = 0L,
    public val isCompleted: Boolean = false
)
