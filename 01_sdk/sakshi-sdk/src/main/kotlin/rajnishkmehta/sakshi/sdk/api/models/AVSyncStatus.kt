package rajnishkmehta.sakshi.sdk.api.models

/**
 * Detailed status update regarding audio/video synchronization reported by Vault.
 *
 * @property fileId Unique identifier of audio/video recording.
 * @property state Current state of synchronization [State].
 * @property lastCopiedOffsetBytes Number of bytes copied to Vault so far.
 * @property totalBytes Optional total size of file if known, or -1 if recording is ongoing.
 * @property isCompleted True if recording has ended and synchronization is finalized.
 * @property message Additional informational message or error string.
 */
public data class AVSyncStatus(
    public val fileId: String,
    public val state: State,
    public val lastCopiedOffsetBytes: Long = 0L,
    public val totalBytes: Long = -1L,
    public val isCompleted: Boolean = false,
    public val message: String? = null
) {
    /**
     * State of audio/video sync operation.
     */
    public enum class State {
        /** Sync request received, waiting for initial copy pass. */
        INITIALIZING,

        /** Vault is actively copying newly written bytes. */
        SYNCING,

        /** Vault scheduler is waiting for next incremental sync cycle. */
        IDLE_WAITING,

        /** Sync has been temporarily paused by client request. */
        PAUSED,

        /** Sync has been explicitly stopped by client request. */
        STOPPED,

        /** Recording ended and all bytes have been copied successfully. */
        COMPLETED,

        /** Synchronization encountered an unrecoverable failure. */
        FAILED
    }
}
