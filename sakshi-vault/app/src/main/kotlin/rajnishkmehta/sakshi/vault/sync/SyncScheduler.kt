package rajnishkmehta.sakshi.vault.sync

import android.content.Context
import android.net.Uri
import rajnishkmehta.sakshi.vault.AppLog as Log
import kotlinx.coroutines.*
import rajnishkmehta.sakshi.vault.db.MediaRecord
import rajnishkmehta.sakshi.vault.db.VaultDatabase
import rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultCallback
import rajnishkmehta.sakshi.sdk.api.models.CopyDoneAck
import rajnishkmehta.sakshi.sdk.api.models.VideoSyncStatus
import rajnishkmehta.sakshi.sdk.api.vault.VaultResponder
import rajnishkmehta.sakshi.sdk.api.SakshiError
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Adaptive scheduler that manages non-overlapping synchronization loops for recording files.
 * It uses Kotlin Coroutines to run periodic copy passes off the main thread, applies
 * recording completion heuristics, and manages client callbacks.
 */
class SyncScheduler(
    private val context: Context,
    private val database: VaultDatabase,
    private val copyEngine: CopyEngine,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val tag = "SyncScheduler"
    private val coroutineScope = CoroutineScope(dispatcher + SupervisorJob())
    private val activeJobs = ConcurrentHashMap<String, Job>()
    private val activeCallbacks = ConcurrentHashMap<String, ISakshiVaultCallback>()

    /**
     * Registers or updates a callback for an active or pending fileId.
     */
    fun registerCallback(fileId: String, callback: ISakshiVaultCallback) {
        activeCallbacks[fileId] = callback
    }

    private val syncIntervalMs = 2000L
    private val consecutiveNoBytesLimit = 5
    private val additionalChecksLimit = 3
    private val additionalCheckDelayMs = 4000L

    /**
     * Starts a periodic synchronization loop for the given [fileId], [sourceUri] and [mimeType].
     * If a synchronization loop is already running for this [fileId], it does nothing.
     */
    fun startSync(fileId: String, sourceUri: String, mimeType: String?, callback: ISakshiVaultCallback) {
        activeCallbacks[fileId] = callback

        if (activeJobs.containsKey(fileId)) {
            Log.d(tag, "Sync already active for fileId: $fileId")
            return
        }

        val job = coroutineScope.launch {
            try {
                runSyncLoop(fileId, sourceUri, mimeType)
            } catch (e: Exception) {
                Log.e(tag, "Error in sync loop for $fileId", e)
                val sakshiError = SakshiError.Unknown(
                    "Sync loop failed: ${e.message}",
                    e
                )
                VaultResponder.sendError(callback, sakshiError)
                updateDatabaseState(fileId, "FAILED")
            } finally {
                activeJobs.remove(fileId)
            }
        }
        activeJobs[fileId] = job
    }

    /**
     * Explicitly stops synchronization for the given [fileId] (usually triggered when recording stops).
     * It performs one final sync pass to ensure all trailing bytes are fully copied before shutting down.
     */
    fun stopSync(fileId: String, callback: ISakshiVaultCallback?) {
        val job = activeJobs.remove(fileId)
        val storedCallback = callback ?: activeCallbacks[fileId]

        coroutineScope.launch {
            job?.cancelAndJoin()

            // Run one final sync pass to copy any last bytes written by the camera
            try {
                val record = database.mediaRecordDao().getRecord(fileId)
                if (record != null && record.completionState != "COMPLETED") {
                    Log.d(tag, "Performing final sync pass for $fileId")
                    copyEngine.copyMediaIncremental(fileId, record.originalUri, record.mimeType)

                    val updatedRecord = database.mediaRecordDao().getRecord(fileId)
                    if (updatedRecord != null) {
                        val finalRecord = updatedRecord.copy(
                            completionState = "COMPLETED",
                            updatedTime = System.currentTimeMillis()
                        )
                        database.mediaRecordDao().insertRecord(finalRecord)

                        if (storedCallback != null) {
                            val uriParsed = finalRecord.vaultUri?.let { Uri.parse(it) }
                            VaultResponder.sendVideoSyncStatus(
                                storedCallback,
                                VideoSyncStatus(
                                    fileId = fileId,
                                    state = VideoSyncStatus.State.COMPLETED,
                                    lastCopiedOffsetBytes = finalRecord.lastCopiedOffset,
                                    totalBytes = finalRecord.lastCopiedOffset,
                                    isCompleted = true,
                                    message = "Recording stopped. Sync completed."
                                )
                            )
                            VaultResponder.sendCopyDone(
                                storedCallback,
                                CopyDoneAck(
                                    fileId,
                                    uriParsed,
                                    finalRecord.lastCopiedOffset,
                                    System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to perform final sync pass for $fileId", e)
                if (storedCallback != null) {
                    VaultResponder.sendError(
                        storedCallback,
                        SakshiError.Unknown("Final sync pass failed: ${e.message}", e)
                    )
                }
            } finally {
                activeCallbacks.remove(fileId)
            }
        }
    }

    /**
     * Scans the database for any active recordings that were interrupted (e.g., due to app death or restart)
     * and resumes their synchronization in the background.
     */
    fun resumePendingSyncs() {
        coroutineScope.launch {
            val pendingRecords = database.mediaRecordDao().getAllRecords().filter {
                it.mediaType != "PHOTO" && (it.completionState == "SYNCING" || it.completionState == "INITIALIZING")
            }
            Log.d(tag, "Resuming ${pendingRecords.size} pending syncs after restart")
            for (record in pendingRecords) {
                // Resume without callback (as the original client is gone)
                if (!activeJobs.containsKey(record.fileId)) {
                    val job = launch {
                        try {
                            runSyncLoop(record.fileId, record.originalUri, record.mimeType)
                        } catch (e: Exception) {
                            Log.e(tag, "Resumed sync failed for ${record.fileId}", e)
                            updateDatabaseState(record.fileId, "FAILED")
                        }
                    }
                    activeJobs[record.fileId] = job
                }
            }
        }
    }

    /**
     * Cancels all active synchronization loops immediately.
     */
    fun cancelAll() {
        coroutineScope.cancel()
    }

    /**
     * The main execution loop for synchronization. It implements the adaptive timer,
     * non-overlapping copy passes, and inactive completion heuristics.
     */
    private suspend fun runSyncLoop(fileId: String, sourceUri: String, mimeType: String?) {
        var consecutiveZeroBytePasses = 0
        var isProbingCompletion = false
        var additionalChecksRemaining = additionalChecksLimit

        // 1. Ensure a record exists in the database
        val dao = database.mediaRecordDao()
        val existing = dao.getRecord(fileId)
        if (existing == null) {
            val now = System.currentTimeMillis()
            val mediaType = rajnishkmehta.sakshi.vault.utils.MimeTypeHelper.determineMediaType(mimeType)
            dao.insertRecord(
                MediaRecord(
                    fileId = fileId,
                    originalUri = sourceUri,
                    vaultUri = null,
                    mediaType = mediaType,
                    mimeType = mimeType,
                    completionState = "INITIALIZING",
                    lastCopiedOffset = 0L,
                    createdTime = now,
                    updatedTime = now
                )
            )
        }

        while (currentCoroutineContext().isActive) {
            val startTime = System.currentTimeMillis()

            // Notify client that we are active
            val callback = activeCallbacks[fileId]
            if (callback != null && !isProbingCompletion) {
                val currentRecord = dao.getRecord(fileId)
                val offset = currentRecord?.lastCopiedOffset ?: 0L
                VaultResponder.sendVideoSyncStatus(
                    callback,
                    VideoSyncStatus(
                        fileId = fileId,
                        state = VideoSyncStatus.State.SYNCING,
                        lastCopiedOffsetBytes = offset,
                        totalBytes = offset,
                        isCompleted = false,
                        message = "Synchronizing video bytes."
                    )
                )
            }

            // 2. Perform copy pass
            var copiedBytes = 0L
            var copyError: Exception? = null
            try {
                copiedBytes = copyEngine.copyMediaIncremental(fileId, sourceUri, mimeType)
            } catch (e: Exception) {
                Log.e(tag, "Copy pass failed for $fileId on current attempt", e)
                copyError = e
            }

            if (copyError != null) {
                // If we get consecutive failures, we retry. If limit reached, report.
                consecutiveZeroBytePasses++
            } else if (copiedBytes == 0L) {
                consecutiveZeroBytePasses++
            } else {
                // Reset counters on successful byte copies
                consecutiveZeroBytePasses = 0
                isProbingCompletion = false
                additionalChecksRemaining = additionalChecksLimit
            }

            // 3. Heuristic: Check if recording has finished due to inactivity
            if (consecutiveZeroBytePasses >= consecutiveNoBytesLimit) {
                if (!isProbingCompletion) {
                    Log.d(tag, "No new bytes for $fileId. Entering probing completion mode.")
                    isProbingCompletion = true
                }

                if (additionalChecksRemaining > 0) {
                    additionalChecksRemaining--
                    Log.d(tag, "Probing completion: check remaining = $additionalChecksRemaining")
                } else {
                    // All additional probes returned 0 bytes, assume finished!
                    Log.d(tag, "No new bytes detected after probing. Marking $fileId as COMPLETED.")
                    markSyncAsCompleted(fileId, callback)
                    break
                }
            }

            // 4. Adaptive timing: calculate duration and sleep remaining sync interval
            val duration = System.currentTimeMillis() - startTime
            val currentInterval = if (isProbingCompletion) additionalCheckDelayMs else syncIntervalMs
            val remainingDelay = maxOf(0L, currentInterval - duration)
            delay(remainingDelay)
        }
    }

    private suspend fun markSyncAsCompleted(fileId: String, callback: ISakshiVaultCallback?) {
        val dao = database.mediaRecordDao()
        val record = dao.getRecord(fileId)
        if (record != null) {
            val finalRecord = record.copy(
                completionState = "COMPLETED",
                updatedTime = System.currentTimeMillis()
            )
            dao.insertRecord(finalRecord)

            if (callback != null) {
                val uriParsed = finalRecord.vaultUri?.let { Uri.parse(it) }
                VaultResponder.sendVideoSyncStatus(
                    callback,
                    VideoSyncStatus(
                        fileId = fileId,
                        state = VideoSyncStatus.State.COMPLETED,
                        lastCopiedOffsetBytes = finalRecord.lastCopiedOffset,
                        totalBytes = finalRecord.lastCopiedOffset,
                        isCompleted = true,
                        message = "Recording finished. Sync completed."
                    )
                )
                VaultResponder.sendCopyDone(
                    callback,
                    CopyDoneAck(
                        fileId,
                        uriParsed,
                        finalRecord.lastCopiedOffset,
                        System.currentTimeMillis()
                    )
                )
            }
        }
        activeJobs.remove(fileId)
        activeCallbacks.remove(fileId)
    }

    private suspend fun updateDatabaseState(fileId: String, state: String) {
        val dao = database.mediaRecordDao()
        val record = dao.getRecord(fileId)
        if (record != null) {
            dao.insertRecord(
                record.copy(
                    completionState = state,
                    updatedTime = System.currentTimeMillis()
                )
            )
        }
    }
}
