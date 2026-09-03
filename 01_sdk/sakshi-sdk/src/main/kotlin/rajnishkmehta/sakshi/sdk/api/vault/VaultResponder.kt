package rajnishkmehta.sakshi.sdk.api.vault

import rajnishkmehta.sakshi.sdk.api.SakshiError
import rajnishkmehta.sakshi.sdk.api.models.CopyDoneAck
import rajnishkmehta.sakshi.sdk.api.models.AVSyncStatus
import rajnishkmehta.sakshi.sdk.internal.ipc.AidlMappers
import rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultCallback

/**
 * Utility class for Vault application developers to easily send structured responses,
 * sync status updates, copy completion acknowledgements, and errors back to client applications (e.g. Camera).
 */
public object VaultResponder {

    /**
     * Sends a photo ingestion acknowledgement back to the client application.
     *
     * @param callback The [ISakshiVaultCallback] received in `sendPhoto`.
     * @param ack The [CopyDoneAck] details.
     */
    @JvmStatic
    public fun sendPhotoAck(callback: ISakshiVaultCallback, ack: CopyDoneAck) {
        runCatching {
            callback.onPhotoAck(AidlMappers.toBundle(ack))
        }
    }

    /**
     * Sends a real-time audio/video synchronization status update back to the client application.
     *
     * @param callback The [ISakshiVaultCallback] received in `startAVSync`.
     * @param status The [AVSyncStatus] state and byte progress.
     */
    @JvmStatic
    public fun sendAVSyncStatus(callback: ISakshiVaultCallback, status: AVSyncStatus) {
        runCatching {
            callback.onAVSyncStatus(AidlMappers.toBundle(status))
        }
    }

    /**
     * Sends a file copy completion acknowledgement ([CopyDoneAck]) back to the client application.
     *
     * @param callback The [ISakshiVaultCallback] instance.
     * @param ack The [CopyDoneAck] containing file ID, optional original source URI, and total copied byte count.
     */
    @JvmStatic
    public fun sendCopyDone(callback: ISakshiVaultCallback, ack: CopyDoneAck) {
        runCatching {
            callback.onCopyDone(AidlMappers.toBundle(ack))
        }
    }

    /**
     * Sends an error event back to the client application.
     *
     * @param callback The [ISakshiVaultCallback] instance.
     * @param error The [SakshiError] detailing the failure cause.
     */
    @JvmStatic
    public fun sendError(callback: ISakshiVaultCallback, error: SakshiError) {
        runCatching {
            callback.onError(AidlMappers.toErrorBundle(error))
        }
    }
}
