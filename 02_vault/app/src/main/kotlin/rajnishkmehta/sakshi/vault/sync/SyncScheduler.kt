/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.sync

import android.content.Context
import rajnishkmehta.sakshi.vault.thumbnail.ThumbnailManager
import android.net.Uri
import rajnishkmehta.sakshi.vault.AppLog as Log
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock
import rajnishkmehta.sakshi.vault.db.MediaRecord
import rajnishkmehta.sakshi.vault.db.MediaState
import rajnishkmehta.sakshi.vault.db.VaultDatabase
import rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultCallback
import rajnishkmehta.sakshi.sdk.api.models.CopyDoneAck
import rajnishkmehta.sakshi.sdk.api.models.AVSyncStatus
import rajnishkmehta.sakshi.sdk.api.vault.VaultResponder
import rajnishkmehta.sakshi.sdk.api.SakshiError
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellationException

/**
 * Adaptive scheduler that manages non-overlapping synchronization loops for recording files.
 * It uses Kotlin Coroutines to run periodic copy passes off the main thread, applies
 * recording completion heuristics, and manages client callbacks.
 */
/**
 * Orchestrates media synchronization tasks, managing worker lifecycles, retries, and overall flow.
 *
 * Evaluates sync requests and dispatches them to the [CopyEngine], while also resuming
 * pending syncs after application restarts.
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

    private fun getMutex(fileId: String) = MediaFileLocks.lockFor(fileId)

    private val syncIntervalMs = 2000L
    private val consecutiveNoBytesLimit = 5
    private val additionalChecksLimit = 3
    private val additionalCheckDelayMs = 4000L

    /**
     * Returns `true` while a synchronization loop is running for [fileId].
     * A final sync pass started by [stopSync] holds the file lock instead and keeps an active state.
     */
    fun isSyncActive(fileId: String): Boolean = activeJobs.containsKey(fileId)

    /**
     * Starts a periodic synchronization loop for the given [fileId], [sourceUri] .
     * If a synchronization loop is already running for this [fileId], it does nothing.
     */
    fun startSync(fileId: String, sourceUri: String, mediaType: String, fileExtension: String, callback: ISakshiVaultCallback) {
        activeCallbacks[fileId] = callback

        if (activeJobs.containsKey(fileId)) {
            Log.d(tag, "Sync already active for fileId: $fileId")
            return
        }

        val job = coroutineScope.launch {
            try {
                runSyncLoop(fileId, sourceUri, mediaType, fileExtension)
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e(tag, "Error in sync loop for $fileId", e)
                    val sakshiError = SakshiError.Unknown(
                        "Sync loop failed: ${e.message}",
                        e
                    )
                    VaultResponder.sendError(callback, sakshiError)
                    updateDatabaseState(fileId, MediaState.FAILED)
                }
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
                getMutex(fileId).withLock {
                    val record = database.mediaRecordDao().getRecord(fileId)
                    if (record != null && record.state != MediaState.COMPLETED) {
                        Log.d(tag, "Performing final sync pass for $fileId")
                        copyEngine.copyMediaIncremental(fileId, record.originalUri, record.mediaType, record.fileExtension)

                        val updatedRecord = database.mediaRecordDao().getRecord(fileId)
                        if (updatedRecord != null) {
                            val finalRecord = updatedRecord.copy(
                                state = MediaState.COMPLETED,
                                updatedTime = System.currentTimeMillis()
                            )
                            database.mediaRecordDao().insertRecord(finalRecord)

                            if (storedCallback != null) {
                                val uriParsed = Uri.parse(finalRecord.originalUri)
                                VaultResponder.sendAVSyncStatus(
                                    storedCallback,
                                    AVSyncStatus(
                                        fileId = fileId,
                                        state = AVSyncStatus.State.COMPLETED,
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

                            // Generate thumbnail for explicitly stopped file
                            finalRecord.vaultPath?.let { vaultPath ->
                                ThumbnailManager.generateAndStoreThumbnail(
                                    context,
                                    fileId,
                                    finalRecord.mediaType,
                                    File(vaultPath)
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e(tag, "Failed to perform final sync pass for $fileId", e)
                    if (storedCallback != null) {
                        VaultResponder.sendError(
                            storedCallback,
                            SakshiError.IpcError(message = "Final sync pass failed: ${e.message}", cause = e)
                        )
                    }
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
                it.mediaType != "PHOTO" && (it.state == MediaState.SYNCING || it.state == MediaState.INITIALIZING)
            }
            Log.d(tag, "Resuming ${pendingRecords.size} pending syncs after restart")
            for (record in pendingRecords) {
                // Resume without callback (as the original client is gone)
                if (!activeJobs.containsKey(record.fileId)) {
                    val job = launch {
                        try {
                            runSyncLoop(record.fileId, record.originalUri, record.mediaType, record.fileExtension)
                        } catch (e: Exception) {
                            Log.e(tag, "Resumed sync failed for ${record.fileId}", e)
                            updateDatabaseState(record.fileId, MediaState.FAILED)
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

    suspend fun pauseSync(fileId: String, callback: ISakshiVaultCallback?) {
        Log.d(tag, "Pausing sync for file: $fileId")
        if (callback != null) {
            activeCallbacks[fileId] = callback
        }

        val record = database.mediaRecordDao().getRecord(fileId)
        if (record == null || record.state == MediaState.COMPLETED) {
            val storedCallback = activeCallbacks[fileId]
            if (storedCallback != null) {
                VaultResponder.sendError(
                    storedCallback,
                    SakshiError.InvalidPayload("Cannot pause: record not found or already completed")
                )
            }
            return
        }

        getMutex(fileId).withLock {
            val currentRecord = database.mediaRecordDao().getRecord(fileId)
            if (currentRecord == null || currentRecord.state == MediaState.COMPLETED) {
                val storedCallback = activeCallbacks[fileId]
                if (storedCallback != null) {
                    VaultResponder.sendError(
                        storedCallback,
                        SakshiError.InvalidPayload("Cannot pause: record not found or already completed")
                    )
                }
                return@withLock
            }

            // Drain currently remaining bytes only once
            copyEngine.copyMediaIncremental(fileId, currentRecord.originalUri, currentRecord.mediaType, currentRecord.fileExtension)

            updateDatabaseState(fileId, MediaState.PAUSED)
            val updatedRecord = database.mediaRecordDao().getRecord(fileId)

            val storedCallback = activeCallbacks[fileId]
            if (storedCallback != null) {
                VaultResponder.sendAVSyncStatus(
                    storedCallback,
                    AVSyncStatus(
                        fileId = fileId,
                        state = AVSyncStatus.State.PAUSED,
                        lastCopiedOffsetBytes = updatedRecord?.lastCopiedOffset ?: currentRecord.lastCopiedOffset,
                        totalBytes = -1L,
                        isCompleted = false,
                        message = "Recording paused."
                    )
                )
            }
        }
    }

    suspend fun resumeSync(fileId: String, callback: ISakshiVaultCallback?) {
        Log.d(tag, "Resuming sync for file: $fileId")
        if (callback != null) {
            activeCallbacks[fileId] = callback
        }

        val record = database.mediaRecordDao().getRecord(fileId)
        if (record == null || record.state != MediaState.PAUSED) {
            val storedCallback = activeCallbacks[fileId]
            if (storedCallback != null) {
                VaultResponder.sendError(
                    storedCallback,
                    SakshiError.InvalidPayload("Cannot resume: record not found or not paused")
                )
            }
            return
        }

        getMutex(fileId).withLock {
            val currentRecord = database.mediaRecordDao().getRecord(fileId)
            if (currentRecord == null || currentRecord.state != MediaState.PAUSED) {
                val storedCallback = activeCallbacks[fileId]
                if (storedCallback != null) {
                    VaultResponder.sendError(
                        storedCallback,
                        SakshiError.InvalidPayload("Cannot resume: record not found or not paused")
                    )
                }
                return@withLock
            }

            updateDatabaseState(fileId, MediaState.SYNCING)

            val storedCallback = activeCallbacks[fileId]
            if (storedCallback != null) {
                VaultResponder.sendAVSyncStatus(
                    storedCallback,
                    AVSyncStatus(
                        fileId = fileId,
                        state = AVSyncStatus.State.SYNCING,
                        lastCopiedOffsetBytes = currentRecord.lastCopiedOffset,
                        totalBytes = -1L,
                        isCompleted = false,
                        message = "Recording resumed."
                    )
                )
            }
        }
    }

    fun cancelAll() {
        coroutineScope.cancel()
    }

    /**
     * The main execution loop for synchronization. It implements the adaptive timer,
     * non-overlapping copy passes, and inactive completion heuristics.
     */
    private suspend fun runSyncLoop(fileId: String, sourceUri: String, mediaType: String, fileExtension: String) {
        var consecutiveZeroBytePasses = 0
        var isProbingCompletion = false
        var additionalChecksRemaining = additionalChecksLimit

        // 1. Ensure a record exists in the database
        val dao = database.mediaRecordDao()
        val existing = dao.getRecord(fileId)
        if (existing == null) {
            val now = System.currentTimeMillis()
            dao.insertRecord(
                MediaRecord(
                    fileId = fileId,
                    originalUri = sourceUri,
                    vaultPath = null,
                    mediaType = mediaType,
                    fileExtension = fileExtension,
                    state = MediaState.INITIALIZING,
                    lastCopiedOffset = 0L,
                    createdTime = now,
                    updatedTime = now
                )
            )
        }

        while (currentCoroutineContext().isActive) {
            val currentRecord = dao.getRecord(fileId)
            if (currentRecord?.state == MediaState.PAUSED) {
                delay(syncIntervalMs)
                continue
            }

            val startTime = System.currentTimeMillis()

            // Notify client that we are active
            val callback = activeCallbacks[fileId]
            if (callback != null && !isProbingCompletion) {
                val offset = currentRecord?.lastCopiedOffset ?: 0L
                VaultResponder.sendAVSyncStatus(
                    callback,
                    AVSyncStatus(
                        fileId = fileId,
                        state = AVSyncStatus.State.SYNCING,
                        lastCopiedOffsetBytes = offset,
                        totalBytes = -1L,
                        isCompleted = false,
                        message = "Synchronizing video bytes."
                    )
                )
            }

            // 2. Perform copy pass
            var copiedBytes = 0L
            var copyError: Exception? = null
            try {
                getMutex(fileId).withLock {
                    val checkRecord = dao.getRecord(fileId)
                    val state = checkRecord?.state

                    if (state == MediaState.COMPLETED) {
                        copiedBytes = 0L
                    } else if (state == MediaState.PAUSED) {
                        // Already drained during pauseSync, do not attempt to copy more bytes
                        copiedBytes = 0L
                    } else {
                        copiedBytes = copyEngine.copyMediaIncremental(fileId, sourceUri, mediaType, fileExtension)
                    }
                }
            } catch (e: IllegalArgumentException) {
                Log.e(tag, "Validation failed for $fileId", e)
                dao.deleteRecord(fileId)
                if (callback != null) {
                    val sakshiError = SakshiError.InvalidPayload("Validation failed: ${e.message}")
                    VaultResponder.sendError(callback, sakshiError)
                }
                activeJobs.remove(fileId)
                activeCallbacks.remove(fileId)
                break
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
            val currentRecordCheck = dao.getRecord(fileId)
            if (currentRecordCheck?.state == MediaState.PAUSED) {
                consecutiveZeroBytePasses = 0
                isProbingCompletion = false
                additionalChecksRemaining = additionalChecksLimit
            } else
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
                state = MediaState.COMPLETED,
                updatedTime = System.currentTimeMillis()
            )
            dao.insertRecord(finalRecord)

            if (callback != null) {
                val uriParsed = Uri.parse(finalRecord.originalUri)
                VaultResponder.sendAVSyncStatus(
                    callback,
                    AVSyncStatus(
                        fileId = fileId,
                        state = AVSyncStatus.State.COMPLETED,
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

            // Generate thumbnail for AVSync completed file
            finalRecord.vaultPath?.let { vaultPath ->
                ThumbnailManager.generateAndStoreThumbnail(
                    context,
                    fileId,
                    finalRecord.mediaType,
                    File(vaultPath)
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
                    state = state,
                    updatedTime = System.currentTimeMillis()
                )
            )
        }
    }
}
