package rajnishkmehta.sakshi.sdk.internal.ipc;

import android.os.Bundle;

/**
 * Asynchronous callback interface provided by Sakshi SDK to Vault application
 * to receive execution results, audio/video sync status updates, copy completion acknowledgements, and error events.
 */
interface ISakshiVaultCallback {
    /**
     * Called when a photo send operation completes or acknowledges.
     *
     * @param responseBundle Bundle containing photo response payload.
     */
    void onPhotoAck(in Bundle responseBundle);

    /**
     * Called when audio/video synchronization status changes.
     *
     * @param syncStatusBundle Bundle containing audio/video sync status updates.
     */
    void onAVSyncStatus(in Bundle syncStatusBundle);

    /**
     * Called when Vault completes copying a recording or file pass.
     *
     * @param copyDoneBundle Bundle containing file ID, optional Vault URI, copied byte count, and timestamp.
     */
    void onCopyDone(in Bundle copyDoneBundle);

    /**
     * Called when an operation encounters an error in Vault.
     *
     * @param errorBundle Bundle containing detailed error code and message.
     */
    void onError(in Bundle errorBundle);
}
