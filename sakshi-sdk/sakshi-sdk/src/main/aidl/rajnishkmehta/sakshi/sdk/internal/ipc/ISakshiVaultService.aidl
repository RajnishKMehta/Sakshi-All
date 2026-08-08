package rajnishkmehta.sakshi.sdk.internal.ipc;

import android.os.Bundle;
import rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultCallback;

/**
 * Remote AIDL interface exposed by Vault application and consumed by Sakshi SDK.
 */
interface ISakshiVaultService {

    /**
     * Performs a health check / ping operation with Vault application.
     *
     * @param requestBundle Parameters for ping (e.g. client timestamp).
     * @return Bundle containing availability status, Vault version, and timestamp.
     */
    Bundle ping(in Bundle requestBundle);

    /**
     * Sends a photo payload/URI reference to Vault for ingestion.
     *
     * @param photoBundle Payload bundle containing photo details.
     * @param callback Callback to receive acknowledgement or errors.
     */
    void sendPhoto(in Bundle photoBundle, in ISakshiVaultCallback callback);

    /**
     * Notifies Vault to start video synchronization for a recording.
     *
     * @param videoSyncBundle Payload bundle containing unique file ID and recording details.
     * @param callback Callback to receive progress updates, acknowledgements, or errors.
     */
    void startVideoSync(in Bundle videoSyncBundle, in ISakshiVaultCallback callback);

    /**
     * Notifies Vault to stop video synchronization for a specific file ID.
     *
     * @param fileId Unique identifier of recording to stop.
     * @param callback Callback to receive confirmation or errors.
     */
    void stopVideoSync(in String fileId, in ISakshiVaultCallback callback);

    /**
     * Queries Vault to determine whether a recording exists or is actively syncing.
     *
     * @param fileId Unique identifier of recording to query.
     * @return Bundle containing query response (existence status, progress, last offset).
     */
    Bundle isRecordingSynced(in String fileId);
}
