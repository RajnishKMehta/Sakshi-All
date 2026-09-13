package rajnishkmehta.sakshi.vault.service

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import rajnishkmehta.sakshi.vault.AppLog as Log
import kotlinx.coroutines.*
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
import rajnishkmehta.sakshi.vault.BuildConfig
import rajnishkmehta.sakshi.vault.thumbnail.ThumbnailManager
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
    private var database: VaultDatabase? = null
    private var storageManager: StorageManager? = null
    private var copyEngine: CopyEngine? = null
    private var scheduler: SyncScheduler? = null
    private var serviceScope: CoroutineScope? = null

    private val binder = object : ISakshiVaultService.Stub() {

        /**
         * Fast-response health check for the vault service.
         */
        override fun ping(requestBundle: Bundle): Bundle {
            Log.d(tag, "Received ping request")
            return Bundle().apply {
                putBoolean("is_available", true)
                putString("vault_version", BuildConfig.VERSION_NAME.toString())
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
                val error = SakshiError.Unknown("Invalid file payload: empty file_id or uri", null)
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
                        fileExtension,
                        File(vaultPath)
                    )
                } catch (e: Exception) {
                    Log.e(tag, "Failed to copy file $fileId", e)
                    VaultResponder.sendError(
                        callback,
                        SakshiError.Unknown("Failed to copy file to vault storage: ${e.message}", e)
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
                val error = SakshiError.Unknown("Invalid video sync payload: empty file_id or uri", null)
                VaultResponder.sendError(callback, error)
                return
            }

            serviceScope?.launch {
                try {
                    rajnishkmehta.sakshi.sdk.api.validation.PathValidator.validatePathComponents(fileId, mediaType, fileExtension)
                    scheduler?.startSync(fileId, sourceUriStr, mediaType, fileExtension, callback)
                } catch (e: IllegalArgumentException) {
                    Log.e(tag, "Invalid path components for startAVSync: ${e.message}", e)
                    // Mark sync failed natively handled since it never starts properly, just return error
                    val error = SakshiError.Unknown("Invalid payload: ${e.message}", e)
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
                val error = SakshiError.Unknown("Invalid stopAVSync payload: empty file_id", null)
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
                val error = SakshiError.Unknown("Invalid pauseAVSync payload: empty file_id", null)
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
                val error = SakshiError.Unknown("Invalid resumeAVSync payload: empty file_id", null)
                VaultResponder.sendError(callback, error)
                return
            }
            serviceScope?.launch {
                scheduler?.resumeSync(fileId, callback)
            }
        }

        override fun isAVSynced(fileId: String): Bundle {
            Log.d(tag, "Received isAVSynced query: fileId=$fileId")
            val record = runBlocking {
                database?.mediaRecordDao()?.getRecord(fileId)
            }

            return Bundle().apply {
                putBoolean("exists", record != null)
                putString("sync_state", record?.completionState ?: "IDLE_WAITING")
                putLong("offset_bytes", record?.lastCopiedOffset ?: 0L)
                putBoolean("is_completed", record?.completionState == "COMPLETED")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "SakshiVaultRemoteService onCreate")
        try {
            serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
            database = VaultDatabase.getDatabase(applicationContext)
            storageManager = AppPrivateStorageManager(applicationContext)
            copyEngine = CopyEngine(applicationContext, database!!, storageManager!!)
            scheduler = SyncScheduler(applicationContext, database!!, copyEngine!!)

            // Automatically resume any interrupted, pending synchronizations from database
            scheduler?.resumePendingSyncs()
            Log.d(tag, "SakshiVaultRemoteService onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(tag, "Error initializing SakshiVaultRemoteService in onCreate: ${e.message}", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(tag, "Client bound to service with intent: $intent")
        return binder
    }

    override fun onDestroy() {
        Log.d(tag, "SakshiVaultRemoteService onDestroy")
        try {
            scheduler?.cancelAll()
            serviceScope?.cancel()
        } catch (e: Exception) {
            Log.e(tag, "Error during onDestroy: ${e.message}", e)
        }
        super.onDestroy()
    }
}
