/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.service

import android.content.Context
import android.net.Uri
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import rajnishkmehta.sakshi.sdk.api.SakshiError
import rajnishkmehta.sakshi.sdk.api.models.CopyDoneAck
import rajnishkmehta.sakshi.sdk.api.vault.VaultResponder
import rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultCallback
import rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultService
import rajnishkmehta.sakshi.vault.AppLog as Log
import rajnishkmehta.sakshi.vault.BuildConfig
import rajnishkmehta.sakshi.vault.db.MediaState
import rajnishkmehta.sakshi.vault.db.VaultDatabase
import rajnishkmehta.sakshi.vault.deletion.DeletionScheduleResult
import rajnishkmehta.sakshi.vault.deletion.MediaDeletionScheduler
import rajnishkmehta.sakshi.vault.recovery.MediaRecoveryManager
import rajnishkmehta.sakshi.vault.deletion.PingCleanupTrigger
import rajnishkmehta.sakshi.vault.sync.CopyEngine
import rajnishkmehta.sakshi.vault.sync.SyncScheduler
import rajnishkmehta.sakshi.vault.thumbnail.ThumbnailManager
import java.io.File
import kotlinx.serialization.json.Json
import rajnishkmehta.sakshi.sdk.api.validation.PathValidator

/**
 * Implementation of the [ISakshiVaultService] AIDL interface.
 * Handles IPC requests and delegates them to internal Vault components.
 */
class SakshiVaultServiceBinder(
    private val applicationContext: Context,
    private val database: VaultDatabase?,
    private val copyEngine: CopyEngine?,
    private val scheduler: SyncScheduler?,
    private val serviceScope: CoroutineScope?,
    private val deletionScheduler: MediaDeletionScheduler? = null,
    private val pingCleanupTrigger: PingCleanupTrigger? = null,
    private val recoveryManager: MediaRecoveryManager? = null
) : ISakshiVaultService.Stub() {

    private val tag = "SakshiVaultServiceBinder"

    /**
     * Fast-response health check for the vault service.
     * Also starts a non-blocking fallback cleanup of media whose deletion retention period elapsed.
     */
    override fun ping(requestBundle: Bundle): Bundle {
        Log.d(tag, "Received ping request")
        try {
            pingCleanupTrigger?.onPing()
        } catch (e: Exception) {
            Log.e(tag, "Failed to start ping-triggered cleanup", e)
        }
        return Bundle().apply {
            putBoolean("is_available", true)
            putString("vault_version", BuildConfig.VERSION_NAME)
            putLong("timestamp", System.currentTimeMillis())
        }
    }

    /**
     * Receives a completed file, copies it to private vault storage,
     * persists metadata in the database, and acknowledges success.
     */
    override fun copyFile(fileBundle: Bundle, callback: ISakshiVaultCallback) {
        val fileId = fileBundle.getString("file_id") ?: ""
        val uriStr = fileBundle.getString("uri") ?: ""
        val mediaType = fileBundle.getString("media_type") ?: "OTHER"
        val fileExtension = fileBundle.getString("file_extension") ?: "bin"
        Log.d(tag, "Received copyFile request: fileId=$fileId, uri=$uriStr, mediaType=$mediaType, fileExtension=$fileExtension")

        if (fileId.isEmpty() || uriStr.isEmpty()) {
            val error = SakshiError.InvalidPayload("Invalid file payload: empty file_id or uri")
            VaultResponder.sendError(callback, error)
            return
        }

        serviceScope?.launch {
            try {
                val vaultPath = copyEngine?.copyFile(fileId, uriStr, mediaType, fileExtension) ?: throw IllegalStateException("CopyEngine not initialized")
                val fileLength = File(vaultPath).length()

                val response = CopyDoneAck(
                    fileId,
                    Uri.parse(uriStr),
                    fileLength,
                    System.currentTimeMillis()
                )
                VaultResponder.sendFileCopyAck(callback, response)
                Log.d(tag, "Successfully copied file $fileId to vault storage")

                // Generate thumbnail
                ThumbnailManager.generateAndStoreThumbnail(
                    applicationContext,
                    fileId,
                    mediaType,
                    File(vaultPath)
                )
            } catch (e: Exception) {
                Log.e(tag, "Failed to copy file $fileId", e)
                VaultResponder.sendError(
                    callback,
                    SakshiError.IpcError(message = "Failed to copy file to vault storage: ${e.message}", cause = e)
                )
            }
        }
    }

    /**
     * Registers a video file and schedules an adaptive, non-overlapping periodic synchronization loop.
     */
    override fun startAVSync(avSyncBundle: Bundle, callback: ISakshiVaultCallback) {
        val fileId = avSyncBundle.getString("file_id") ?: ""
        val sourceUriStr = avSyncBundle.getString("uri") ?: ""
        val mediaType = avSyncBundle.getString("media_type") ?: "OTHER"
        val fileExtension = avSyncBundle.getString("file_extension") ?: "bin"
        Log.d(tag, "Received startAVSync request: fileId=$fileId, uri=$sourceUriStr, mediaType=$mediaType, fileExtension=$fileExtension")

        if (fileId.isEmpty() || sourceUriStr.isEmpty()) {
            val error = SakshiError.InvalidPayload("Invalid video sync payload: empty file_id or uri")
            VaultResponder.sendError(callback, error)
            return
        }

        serviceScope?.launch {
            try {
                PathValidator.validatePathComponents(fileId, mediaType, fileExtension)
                scheduler?.startSync(fileId, sourceUriStr, mediaType, fileExtension, callback)
            } catch (e: IllegalArgumentException) {
                Log.e(tag, "Invalid path components for startAVSync: ${e.message}", e)
                // Mark sync failed natively handled since it never starts properly, just return error
                val error = SakshiError.InvalidPayload("Invalid payload: ${e.message}")
                VaultResponder.sendError(callback, error)
            }
        }
    }

    /**
     * Cancels the active sync loop for the video and schedules a final incremental copy
     * to sync any remaining trailing bytes before marking as complete.
     */
    override fun stopAVSync(fileId: String, callback: ISakshiVaultCallback) {
        Log.d(tag, "Received stopAVSync request: fileId=$fileId")
        if (fileId.isEmpty()) {
            val error = SakshiError.InvalidPayload("Invalid stopAVSync payload: empty file_id")
            VaultResponder.sendError(callback, error)
            return
        }

        serviceScope?.launch {
            scheduler?.stopSync(fileId, callback)
        }
    }

    /**
     * Synchronously queries the database to report the current synchronization progress and state.
     */

    override fun pauseAVSync(fileId: String, callback: ISakshiVaultCallback) {
        Log.d(tag, "Received pauseAVSync request: fileId=${fileId}")
        if (fileId.isEmpty()) {
            val error = SakshiError.InvalidPayload("Invalid pauseAVSync payload: empty file_id")
            VaultResponder.sendError(callback, error)
            return
        }
        serviceScope?.launch {
            scheduler?.pauseSync(fileId, callback)
        }
    }

    override fun resumeAVSync(fileId: String, callback: ISakshiVaultCallback) {
        Log.d(tag, "Received resumeAVSync request: fileId=${fileId}")
        if (fileId.isEmpty()) {
            val error = SakshiError.InvalidPayload("Invalid resumeAVSync payload: empty file_id")
            VaultResponder.sendError(callback, error)
            return
        }
        serviceScope?.launch {
            scheduler?.resumeSync(fileId, callback)
        }
    }

    override fun listMedia(): Bundle {
        Log.d(tag, "Received listMedia query")
        val records = runBlocking {
            val db = database ?: throw IllegalStateException("VaultDatabase is not initialized")
            db.mediaRecordDao().getAllRecords()
        }

        val validRecords = filterListableRecords(records)
        val jsonString = formatMediaList(validRecords) { it.createdTime }

        return Bundle().apply {
            putString("media_list_json", jsonString)
        }
    }

    override fun listDeletedMedia(): Bundle {
        Log.d(tag, "Received listDeletedMedia query")
        val records = runBlocking {
            val db = database ?: throw IllegalStateException("VaultDatabase is not initialized")
            db.mediaRecordDao().getRecordsInState(MediaState.DELETE)
        }

        val jsonString = formatMediaList(records) {
            it.deletionScheduledAt ?: it.updatedTime
        }

        return Bundle().apply {
            putString("media_list_json", jsonString)
        }
    }

    override fun isAVSynced(fileId: String): Bundle {
        Log.d(tag, "Received isAVSynced query: fileId=$fileId")
        val record = runBlocking {
            database?.mediaRecordDao()?.getRecord(fileId)
        }

        return Bundle().apply {
            putBoolean("exists", record != null)
            putString("sync_state", record?.state ?: "IDLE_WAITING")
            putLong("offset_bytes", record?.lastCopiedOffset ?: 0L)
            putBoolean("is_completed", record?.state == MediaState.COMPLETED)
        }
    }

    override fun getMedia(fileId: String): Bundle {
        Log.d(tag, "Received getMedia query for fileId=$fileId")

        val record = runBlocking {
            database?.mediaRecordDao()?.getRecord(fileId)
        }

        return Bundle().apply {
            if (record != null) {
                val mediaTypeLower = record.mediaType.lowercase()
                putString("contentUri", "content://${applicationContext.packageName}.mediaprovider/media/$mediaTypeLower/$fileId")
                putString("fileId", record.fileId)
                putString("mediaType", record.mediaType)
                putString("fileExtension", record.fileExtension)
                putLong("createdTime", record.createdTime)
            } else {
                putString("error", "Media file not found for id: $fileId")
            }
        }
    }

    /**
     * Schedules a single media file for permanent deletion after the retention period.
     * The file is not deleted immediately; it is hidden from listings once scheduling succeeds.
     */
    override fun deleteMedia(fileId: String?): Bundle {
        Log.d(tag, "Received deleteMedia request: fileId=$fileId")
        return try {
            val deletion = deletionScheduler ?: throw IllegalStateException("Deletion scheduler is not initialized")
            when (val result = runBlocking { deletion.schedule(fileId.orEmpty()) }) {
                is DeletionScheduleResult.Scheduled -> Bundle().apply {
                    putBoolean("success", true)
                    putLong("deletionScheduledAt", result.scheduledAt)
                }
                is DeletionScheduleResult.Rejected -> errorBundle(result.reason)
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to schedule deletion for $fileId", e)
            errorBundle("Failed to schedule deletion: ${e.message}")
        }
    }

    /**
     * Schedules multiple media files for permanent deletion after the retention period and reports
     * per-file results in `successfulIds` and `failedIds`.
     */
    override fun deleteMediaBatch(fileIds: MutableList<String>?): Bundle {
        Log.d(tag, "Received deleteMediaBatch request for ${fileIds?.size ?: 0} files")
        return try {
            val deletion = deletionScheduler ?: throw IllegalStateException("Deletion scheduler is not initialized")
            val ids = fileIds?.toList().orEmpty()
            if (ids.isEmpty()) return errorBundle("fileIds must not be empty")
            val result = runBlocking { deletion.scheduleBatch(ids) }
            Bundle().apply {
                putStringArrayList("successfulIds", ArrayList(result.successfulIds))
                putBundle("failedIds", Bundle().apply {
                    result.failedIds.forEach { (id, reason) -> putString(id, reason) }
                })
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to schedule batch deletion", e)
            errorBundle("Failed to schedule batch deletion: ${e.message}")
        }
    }

    private fun errorBundle(message: String): Bundle = Bundle().apply {
        putBoolean("success", false)
        putString("error", message)
    }


    override fun recoverMedia(fileId: String?): Bundle {
        Log.d(tag, "Received recoverMedia request: fileId=$fileId")
        return try {
            val manager = recoveryManager ?: throw IllegalStateException("Recovery manager is not initialized")
            when (val result = runBlocking { manager.recover(fileId.orEmpty()) }) {
                is MediaRecoveryManager.RecoveryResult.Recovered -> Bundle().apply {
                    putBoolean("success", true)
                }
                is MediaRecoveryManager.RecoveryResult.Rejected -> errorBundle(result.reason)
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to recover media for $fileId", e)
            errorBundle("Failed to recover media: ${e.message}")
        }
    }

    override fun recoverMediaBatch(fileIds: MutableList<String>?): Bundle {
        Log.d(tag, "Received recoverMediaBatch request for ${fileIds?.size ?: 0} files")
        return try {
            val manager = recoveryManager ?: throw IllegalStateException("Recovery manager is not initialized")
            val ids = fileIds?.toList().orEmpty()
            if (ids.isEmpty()) return errorBundle("fileIds must not be empty")
            val result = runBlocking { manager.recoverBatch(ids) }
            Bundle().apply {
                putStringArrayList("successfulIds", ArrayList(result.successfulIds))
                putBundle("failedIds", Bundle().apply {
                    result.failedIds.forEach { (id, reason) -> putString(id, reason) }
                })
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to recover batch media", e)
            errorBundle("Failed to recover batch media: ${e.message}")
        }
    }

    override fun getThumbnail(): String {
        Log.d(tag, "Received getThumbnail query")
        return "content://${applicationContext.packageName}.thumbnailprovider/media/{mediaType}/thumbnail/{fileId}.webp"
    }
}
