package rajnishkmehta.sakshi.sdk.api

import android.content.Context
import kotlinx.coroutines.flow.Flow
import rajnishkmehta.sakshi.sdk.api.models.CopyDoneAck
import rajnishkmehta.sakshi.sdk.api.models.PhotoRequest
import rajnishkmehta.sakshi.sdk.api.models.RecordingQueryResponse
import rajnishkmehta.sakshi.sdk.api.models.VaultPingResponse
import rajnishkmehta.sakshi.sdk.api.models.AVSyncRequest
import rajnishkmehta.sakshi.sdk.api.models.AVSyncStatus
import rajnishkmehta.sakshi.sdk.internal.SakshiClientImpl

/**
 * Public client interface for Sakshi SDK.
 *
 * Exposes clean, asynchronous Kotlin APIs to communicate with the Vault application.
 * Hides all underlying Binder, IPC, AIDL, and Service Connection details.
 */
public interface SakshiClient {

    /**
     * Performs a ping health check against Vault to verify connection and service availability.
     *
     * @return [SakshiResult] containing [VaultPingResponse] on success or [SakshiError] on failure.
     */
    public suspend fun pingVault(): SakshiResult<VaultPingResponse>

    /**
     * Sends a photo payload to Vault for ingestion.
     *
     * @param request [PhotoRequest] detailing photo URI, ID, and metadata.
     * @return [SakshiResult] containing [CopyDoneAck] acknowledgement or [SakshiError] failure.
     */
    public suspend fun sendPhoto(request: PhotoRequest): SakshiResult<CopyDoneAck>

    /**
     * Initiates audio/video synchronization for a recording in Vault.
     *
     * Returns a cold [Flow] that emits real-time [AVSyncStatus] updates until synchronization
     * completes, fails, or is stopped.
     *
     * @param request [AVSyncRequest] detailing unique file ID, Uri, and metadata.
     * @return A [Flow] emitting [SakshiResult] containing [AVSyncStatus] updates.
     */
    public fun startAVSync(request: AVSyncRequest): Flow<SakshiResult<AVSyncStatus>>

    /**
     * Observes completion acknowledgements ([CopyDoneAck]) sent by Vault when a file copy operation finishes.
     *
     * @param fileId Unique identifier of audio/video recording to observe.
     * @return A [Flow] emitting [SakshiResult] containing [CopyDoneAck].
     */
    public fun observeCopyDone(fileId: String): Flow<SakshiResult<CopyDoneAck>>

    /**
     * Requests Vault to stop audio/video synchronization for a specific file ID.
     *
     * @param fileId Unique identifier of audio/video recording.
     * @return [SakshiResult] containing [Unit] on success or [SakshiError] on failure.
     */
    public suspend fun stopAVSync(fileId: String): SakshiResult<Unit>

    /**
     * Requests Vault to temporarily pause audio/video synchronization for a specific file ID.
     *
     * @param fileId Unique identifier of audio/video recording.
     * @return [SakshiResult] containing [Unit] on success or [SakshiError] on failure.
     */
    public suspend fun pauseAVSync(fileId: String): SakshiResult<Unit>

    /**
     * Requests Vault to resume audio/video synchronization for a specific file ID.
     *
     * @param fileId Unique identifier of audio/video recording.
     * @return [SakshiResult] containing [Unit] on success or [SakshiError] on failure.
     */
    public suspend fun resumeAVSync(fileId: String): SakshiResult<Unit>

    /**
     * Queries Vault to check whether a recording with [fileId] exists or is syncing.
     *
     * @param fileId Unique identifier of audio/video recording.
     * @return [SakshiResult] containing [RecordingQueryResponse] or [SakshiError] on failure.
     */
    public suspend fun isAVSynced(fileId: String): SakshiResult<RecordingQueryResponse>

    /**
     * Explicitly disconnects from Vault IPC service and releases internal resources.
     */
    public fun disconnect()

    public companion object {
        /**
         * Creates and initializes a new [SakshiClient] instance.
         *
         * @param context Application or component Context.
         * @param config Optional [SakshiClientConfig] customization.
         * @return An instance of [SakshiClient].
         */
        @JvmStatic
        @JvmOverloads
        public fun create(
            context: Context,
            config: SakshiClientConfig = SakshiClientConfig()
        ): SakshiClient {
            return SakshiClientImpl(
                context = context.applicationContext,
                config = config
            )
        }
    }
}
