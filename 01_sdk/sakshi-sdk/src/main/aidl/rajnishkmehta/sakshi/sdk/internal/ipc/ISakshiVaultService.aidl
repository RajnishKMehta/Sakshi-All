/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
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
     * Sends a file payload/URI reference to Vault for ingestion.
     *
     * @param fileBundle Payload bundle containing file details.
     * @param callback Callback to receive acknowledgement or errors.
     */
    void copyFile(in Bundle fileBundle, in ISakshiVaultCallback callback);


    /**
     * Notifies Vault to start audio/video synchronization for a recording.
     *
     * @param avSyncBundle Payload bundle containing unique file ID and recording details.
     * @param callback Callback to receive progress updates, acknowledgements, or errors.
     */
    void startAVSync(in Bundle avSyncBundle, in ISakshiVaultCallback callback);


    /**
     * Notifies Vault to stop audio/video synchronization for a specific file ID.
     *
     * @param fileId Unique identifier of recording to stop.
     * @param callback Callback to receive confirmation or errors.
     */
    void stopAVSync(in String fileId, in ISakshiVaultCallback callback);


    /**
     * Notifies Vault to pause audio/video synchronization for a specific file ID.
     *
     * @param fileId Unique identifier of recording to pause.
     * @param callback Callback to receive confirmation or errors.
     */
    void pauseAVSync(in String fileId, in ISakshiVaultCallback callback);


    /**
     * Notifies Vault to resume audio/video synchronization for a specific file ID.
     *
     * @param fileId Unique identifier of recording to resume.
     * @param callback Callback to receive confirmation or errors.
     */
    void resumeAVSync(in String fileId, in ISakshiVaultCallback callback);


    /**
     * Queries Vault to determine whether an audio/video recording exists or is actively syncing.
     *
     * @param fileId Unique identifier of recording to query.
     * @return Bundle containing query response (existence status, progress, last offset).
     */
    Bundle isAVSynced(in String fileId);


    /**
     * Lists all stored media files grouped by media type in JSON format.
     */
    Bundle listMedia();

    /**
     * Lists all stored media files scheduled for deletion grouped by media type in JSON format.
     */
    Bundle listDeletedMedia();


    /**
     * Retrieves the ContentProvider URI template for accessing media thumbnails.
     * The returned template will contain {mediaType} and {fileId} placeholders.
     */
    String getThumbnail();

    /**
     * Retrieves media file details including the real ContentProvider URI for accessing a specific media file.
     */
    Bundle getMedia(in String fileId);

    /**
     * Schedules a specific media file for deletion in the Vault. The file is permanently
     * deleted after the Vault's retention period, not immediately.
     *
     * @param fileId Unique identifier of the media file to schedule for deletion.
     * @return Bundle containing success status or error details.
     */
    Bundle deleteMedia(in String fileId);

    /**
     * Schedules multiple media files for deletion in the Vault in a single batch.
     *
     * @param fileIds List of unique identifiers of the media files to schedule for deletion.
     * @return Bundle containing the lists of successfully scheduled and failed files.
     */
    Bundle deleteMediaBatch(in List<String> fileIds);

    /**
     * Recovers a specific media file previously scheduled for deletion in the Vault.
     *
     * @param fileId Unique identifier of the media file to recover.
     * @return Bundle containing success status or error details.
     */
    Bundle recoverMedia(in String fileId);

    /**
     * Recovers multiple media files previously scheduled for deletion in the Vault in a single batch.
     *
     * @param fileIds List of unique identifiers of the media files to recover.
     * @return Bundle containing the lists of successfully recovered and failed files.
     */
    Bundle recoverMediaBatch(in List<String> fileIds);
}
