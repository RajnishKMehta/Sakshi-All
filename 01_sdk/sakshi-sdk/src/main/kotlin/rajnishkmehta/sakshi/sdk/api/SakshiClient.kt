/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.sdk.api

import android.content.Context
import kotlinx.coroutines.flow.Flow
import rajnishkmehta.sakshi.sdk.api.models.CopyDoneAck
import rajnishkmehta.sakshi.sdk.api.models.FileCopyRequest
import rajnishkmehta.sakshi.sdk.api.models.RecordingQueryResponse
import rajnishkmehta.sakshi.sdk.api.models.BatchMediaDeleteResponse
import rajnishkmehta.sakshi.sdk.api.models.BatchMediaRecoverResponse
import rajnishkmehta.sakshi.sdk.api.models.MediaDetailsResponse
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
     * Sends a file payload to Vault for ingestion.
     *
     * @param request [FileCopyRequest] detailing file URI, ID, and metadata.
     * @return [SakshiResult] containing [CopyDoneAck] acknowledgement or [SakshiError] failure.
     */
    public suspend fun copyFile(request: FileCopyRequest): SakshiResult<CopyDoneAck>

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
     * Requests Vault to stop audio/video synchronization for a specific file ID.
     *
     * @param fileId Unique identifier of audio/video recording.
     * @return [SakshiResult] containing [CopyDoneAck] on success or [SakshiError] on failure.
     */
    public suspend fun stopAVSync(fileId: String): SakshiResult<CopyDoneAck>

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
     * Lists all stored media files grouped by media type in JSON format.
     *
     * @return [SakshiResult] containing the raw JSON string on success or [SakshiError] on failure.
     */
    public suspend fun listMedia(): SakshiResult<String>

    /**
     * Lists all stored media files scheduled for deletion grouped by media type in JSON format.
     *
     * Returns only media whose state is scheduled for deletion. Normally listable media are excluded.
     *
     * @return [SakshiResult] containing the raw JSON string on success or [SakshiError] on failure.
     */
    public suspend fun listDeletedMedia(): SakshiResult<String>

    /**
     * Retrieves the details of a specific media file including its ContentResolver URI.
     *
     * @param fileId The unique identifier of the media.
     * @return [SakshiResult] containing [MediaDetailsResponse] on success or [SakshiError] on failure.
     */
    public suspend fun getMedia(fileId: String): SakshiResult<MediaDetailsResponse>

    /**
     * Retrieves the ContentProvider URI template for accessing media thumbnails.
     *
     * @return [SakshiResult] containing the URI template string on success or [SakshiError] on failure.
     */

    public suspend fun getThumbnail(): SakshiResult<String>

    /**
     * Schedules a specific media file for deletion in Vault.
     *
     * Vault does not delete the file immediately: it hides the file from `listMedia` (other queries
     * still return it, with sync state [AVSyncStatus.State.DELETE]) and permanently deletes its
     * private copy after a 30-day retention period. Repeated requests for an already scheduled file
     * succeed without extending the retention period. Files that are still being synchronized are
     * rejected.
     *
     * @param fileId The unique identifier of the media.
     * @return [SakshiResult] containing [Unit] once Vault accepted the request, or [SakshiError] on failure.
     */
    public suspend fun deleteMedia(fileId: String): SakshiResult<Unit>

    /**
     * Schedules multiple media files for deletion in Vault in a single batch.
     *
     * Each file is handled like the single-file [deleteMedia] overload. Per-file results are
     * reported in [BatchMediaDeleteResponse.successfulIds] and [BatchMediaDeleteResponse.failedIds].
     *
     * @param fileIds The list of unique identifiers of the media files to schedule for deletion.
     * @return [SakshiResult] containing [BatchMediaDeleteResponse] on success or [SakshiError] on failure.
     */
    public suspend fun deleteMedia(fileIds: List<String>): SakshiResult<BatchMediaDeleteResponse>

    /**
     * Recovers a specific media file previously scheduled for deletion in Vault.
     *
     * Vault restores the file to its valid state, preventing its permanent deletion and making
     * it visible in `listMedia` again. Requests for files that were never scheduled for deletion
     * succeed without making any changes. Requests for invalid IDs or files that have already been
     * permanently deleted are rejected.
     *
     * @param fileId The unique identifier of the media to recover.
     * @return [SakshiResult] containing [Unit] once Vault accepted the request, or [SakshiError] on failure.
     */
    public suspend fun recoverMedia(fileId: String): SakshiResult<Unit>

    /**
     * Recovers multiple media files previously scheduled for deletion in Vault in a single batch.
     *
     * Each file is handled like the single-file [recoverMedia] overload. Per-file results are
     * reported in [BatchMediaRecoverResponse.successfulIds] and [BatchMediaRecoverResponse.failedIds].
     *
     * @param fileIds The list of unique identifiers of the media files to recover.
     * @return [SakshiResult] containing [BatchMediaRecoverResponse] on success or [SakshiError] on failure.
     */
    public suspend fun recoverMedia(fileIds: List<String>): SakshiResult<BatchMediaRecoverResponse>

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
