package rajnishkmehta.sakshi.vault.service

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import rajnishkmehta.sakshi.vault.AppLog as Log
import kotlinx.coroutines.*
// import rajnishkmehta.sakshi.vault.BuildConfig *don't remove*
import rajnishkmehta.sakshi.vault.db.VaultDatabase
import rajnishkmehta.sakshi.vault.storage.AppPrivateStorageManager
import rajnishkmehta.sakshi.vault.storage.StorageManager
import rajnishkmehta.sakshi.vault.sync.CopyEngine
import rajnishkmehta.sakshi.vault.sync.SyncScheduler
import rajnishkmehta.sakshi.sdk.api.models.CopyDoneAck
import rajnishkmehta.sakshi.sdk.api.vault.VaultResponder
import rajnishkmehta.sakshi.sdk.api.SakshiError
import rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultCallback
import rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultService
import java.io.File

/**
 * Remote IPC background service that implements the [ISakshiVaultService] AIDL interface.
 * It coordinates incoming photo and video synchronization requests from client applications
 * using the Sakshi SDK.
 *
 * This service is designed strictly as a background headless component with no launcher icon or UI.
 * All long-running operations are offloaded from Binder threads to a background coroutine scope
 * to ensure that client requests are non-blocking and the IPC layer remains highly responsive.
 */
class SakshiVaultRemoteService : Service() {

    private val tag = "SakshiVaultService"
    private lateinit var database: VaultDatabase
    private lateinit var storageManager: StorageManager
    private lateinit var copyEngine: CopyEngine
    private lateinit var scheduler: SyncScheduler
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Volatile
    private var isInitialized = false

    @Synchronized
    private fun ensureInitialized() {
        if (!isInitialized) {
            database = VaultDatabase.getDatabase(this)
            storageManager = AppPrivateStorageManager(this)
            copyEngine = CopyEngine(this, database, storageManager)
            scheduler = SyncScheduler(this, database, copyEngine)
            isInitialized = true
        }
    }

    private val binder = object : ISakshiVaultService.Stub() {

        /**
         * Fast-response health check for the vault service.
         */
        override fun ping(requestBundle: Bundle): Bundle {
            Log.d(tag, "Received ping request")
            return try {
                ensureInitialized()
                Bundle().apply {
                    putBoolean("is_available", true)
                    putString("vault_version", "1.0.0")
                    putLong("timestamp", System.currentTimeMillis())
                }
            } catch (e: Throwable) {
                Log.e(tag, "Error processing ping request", e)
                Bundle().apply {
                    putBoolean("is_available", false)
                    putString("error_message", "Vault service initialization or ping failed: ${e.message}")
                }
            }
        }

        /**
         * Receives a completed photo, copies it to private vault storage,
         * persists metadata in the database, and acknowledges success.
         */
        override fun sendPhoto(photoBundle: Bundle, callback: ISakshiVaultCallback) {
            try {
                ensureInitialized()
                val fileId = photoBundle.getString("file_id") ?: ""
                val uriStr = photoBundle.getString("uri") ?: ""
                val mimeType = photoBundle.getString("mime_type")
                Log.d(tag, "Received sendPhoto request: fileId=$fileId, uri=$uriStr, mimeType=$mimeType")

                if (fileId.isEmpty() || uriStr.isEmpty()) {
                    val error = SakshiError.InvalidPayload("Invalid photo payload: empty file_id or uri")
                    VaultResponder.sendError(callback, error)
                    return
                }

                serviceScope.launch {
                    try {
                        val vaultUriStr = copyEngine.copyPhoto(fileId, uriStr, mimeType)
                        val realPath = vaultUriStr.removePrefix("file://")
                        val fileLength = File(realPath).length()

                        val response = CopyDoneAck(
                            fileId,
                            Uri.parse(vaultUriStr),
                            fileLength,
                            System.currentTimeMillis()
                        )
                        VaultResponder.sendPhotoAck(callback, response)
                        Log.d(tag, "Successfully copied photo $fileId to vault storage")
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to copy photo $fileId", e)
                        VaultResponder.sendError(
                            callback,
                            SakshiError.Unknown("Failed to copy photo to vault storage: ${e.message}", e)
                        )
                    }
                }
            } catch (e: Throwable) {
                Log.e(tag, "Error handling sendPhoto request", e)
                VaultResponder.sendError(
                    callback,
                    SakshiError.IpcError(message = "Vault error handling sendPhoto: ${e.message}", cause = e)
                )
            }
        }

        /**
         * Registers a video file and schedules an adaptive, non-overlapping periodic synchronization loop.
         */
        override fun startVideoSync(videoSyncBundle: Bundle, callback: ISakshiVaultCallback) {
            try {
                ensureInitialized()
                val fileId = videoSyncBundle.getString("file_id") ?: ""
                val sourceUriStr = videoSyncBundle.getString("uri") ?: ""
                val mimeType = videoSyncBundle.getString("mime_type")
                Log.d(tag, "Received startVideoSync request: fileId=$fileId, uri=$sourceUriStr, mimeType=$mimeType")

                if (fileId.isEmpty()) {
                    val error = SakshiError.InvalidPayload("Invalid video sync payload: empty file_id")
                    VaultResponder.sendError(callback, error)
                    return
                }

                if (sourceUriStr.isEmpty()) {
                    // Check if recording already exists or is syncing
                    serviceScope.launch {
                        try {
                            val record = database.mediaRecordDao().getRecord(fileId)
                            if (record != null) {
                                if (record.completionState == "COMPLETED") {
                                    val uriParsed = record.vaultUri?.let { Uri.parse(it) }
                                    VaultResponder.sendCopyDone(
                                        callback,
                                        CopyDoneAck(fileId, uriParsed, record.lastCopiedOffset, System.currentTimeMillis())
                                    )
                                } else {
                                    scheduler.registerCallback(fileId, callback)
                                }
                            } else {
                                VaultResponder.sendError(
                                    callback,
                                    SakshiError.InvalidPayload("Invalid video sync payload: missing 'uri' for file_id '$fileId'")
                                )
                            }
                        } catch (e: Exception) {
                            VaultResponder.sendError(
                                callback,
                                SakshiError.Unknown("Error checking video sync record: ${e.message}", e)
                            )
                        }
                    }
                    return
                }

                serviceScope.launch {
                    try {
                        scheduler.startSync(fileId, sourceUriStr, mimeType, callback)
                    } catch (e: Exception) {
                        Log.e(tag, "Error starting video sync for $fileId", e)
                        VaultResponder.sendError(
                            callback,
                            SakshiError.Unknown("Failed to start video sync: ${e.message}", e)
                        )
                    }
                }
            } catch (e: Throwable) {
                Log.e(tag, "Error handling startVideoSync request", e)
                VaultResponder.sendError(
                    callback,
                    SakshiError.IpcError(message = "Vault error handling startVideoSync: ${e.message}", cause = e)
                )
            }
        }

        /**
         * Cancels the active sync loop for the video and schedules a final incremental copy
         * to sync any remaining trailing bytes before marking as complete.
         */
        override fun stopVideoSync(fileId: String, callback: ISakshiVaultCallback) {
            try {
                ensureInitialized()
                Log.d(tag, "Received stopVideoSync request: fileId=$fileId")
                if (fileId.isEmpty()) {
                    val error = SakshiError.InvalidPayload("Invalid stopVideoSync payload: empty file_id")
                    VaultResponder.sendError(callback, error)
                    return
                }

                serviceScope.launch {
                    try {
                        scheduler.stopSync(fileId, callback)
                    } catch (e: Exception) {
                        Log.e(tag, "Error stopping video sync for $fileId", e)
                        VaultResponder.sendError(
                            callback,
                            SakshiError.Unknown("Failed to stop video sync: ${e.message}", e)
                        )
                    }
                }
            } catch (e: Throwable) {
                Log.e(tag, "Error handling stopVideoSync request", e)
                VaultResponder.sendError(
                    callback,
                    SakshiError.IpcError(message = "Vault error handling stopVideoSync: ${e.message}", cause = e)
                )
            }
        }

        /**
         * Synchronously queries the database to report the current synchronization progress and state.
         */
        override fun isRecordingSynced(fileId: String): Bundle {
            Log.d(tag, "Received isRecordingSynced query: fileId=$fileId")
            return try {
                ensureInitialized()
                val record = runBlocking {
                    database.mediaRecordDao().getRecord(fileId)
                }

                Bundle().apply {
                    putBoolean("exists", record != null)
                    putString("sync_state", record?.completionState ?: "IDLE_WAITING")
                    putLong("offset_bytes", record?.lastCopiedOffset ?: 0L)
                    putBoolean("is_completed", record?.completionState == "COMPLETED")
                }
            } catch (e: Throwable) {
                Log.e(tag, "Error querying isRecordingSynced for $fileId", e)
                Bundle().apply {
                    putBoolean("exists", false)
                    putString("sync_state", "FAILED")
                    putLong("offset_bytes", 0L)
                    putBoolean("is_completed", false)
                    putString("error_message", "Vault error querying sync status: ${e.message}")
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "SakshiVaultRemoteService onCreate")
        try {
            ensureInitialized()
            // Automatically resume any interrupted, pending synchronizations from database
            scheduler.resumePendingSyncs()
        } catch (e: Throwable) {
            Log.e(tag, "Failed to complete onCreate initialization in SakshiVaultRemoteService", e)
        }
    }


    override fun onBind(intent: Intent?): IBinder {
        Log.d(tag, "Client bound to service with intent: $intent")
        return binder
    }

    override fun onDestroy() {
        Log.d(tag, "SakshiVaultRemoteService onDestroy")
        scheduler.cancelAll()
        serviceScope.cancel()
        super.onDestroy()
    }
}
