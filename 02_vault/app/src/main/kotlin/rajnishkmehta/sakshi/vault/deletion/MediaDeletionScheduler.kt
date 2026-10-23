/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.deletion

import kotlinx.coroutines.CancellationException
import rajnishkmehta.sakshi.sdk.api.validation.PathValidator
import rajnishkmehta.sakshi.vault.AppLog as Log
import rajnishkmehta.sakshi.vault.db.MediaRecordDao
import rajnishkmehta.sakshi.vault.db.MediaState
import rajnishkmehta.sakshi.vault.sync.MediaFileLocks

/**
 * Accepts deletion requests for single or multiple media files.
 *
 * Accepting a request only moves the record to [MediaState.DELETE] and records the scheduling
 * time; no file is touched. Permanent deletion happens later through [MediaCleanupCoordinator].
 *
 * Rules:
 * - File IDs must pass [PathValidator.validateFileId]; unknown IDs are rejected.
 * - Only records in [MediaState.DELETABLE_STATES] can be scheduled. Files with an active
 *   synchronization are rejected so they are never deleted while being copied.
 * - Repeated requests for an already scheduled file succeed without changing the original
 *   scheduling timestamp, so retries are safe and never extend the retention period.
 *
 * @param isSyncActive Returns `true` while a synchronization may still write the given file.
 */
/**
 * Coordinates moving media files into the deletion state ([rajnishkmehta.sakshi.vault.db.MediaState.DELETE])
 * and schedules future cleanups.
 *
 * Does not perform the actual file deletion; relies on [MediaCleanupScheduler] to enqueue the cleanup sweep.
 */
class MediaDeletionScheduler(
    private val dao: MediaRecordDao,
    private val clock: DeletionClock,
    private val eligibilityChecker: DeletionEligibilityChecker,
    private val cleanupScheduler: MediaCleanupScheduler,
    private val isSyncActive: (String) -> Boolean
) {

    private val tag = "MediaDeletionScheduler"

    /** Schedules a single file for deletion. */
    suspend fun schedule(fileId: String): DeletionScheduleResult {
        try {
            PathValidator.validateFileId(fileId)
        } catch (e: IllegalArgumentException) {
            return DeletionScheduleResult.Rejected(fileId, "Invalid fileId: ${e.message}")
        }

        val result = MediaFileLocks.withFileLock(fileId) { markScheduled(fileId) }
        if (result is DeletionScheduleResult.Scheduled) {
            enqueueCleanup(result)
        }
        return result
    }

    /**
     * Schedules multiple files for deletion. Each ID is processed independently; duplicates are
     * collapsed and reported once.
     *
     * @throws IllegalArgumentException if [fileIds] is empty.
     */
    suspend fun scheduleBatch(fileIds: List<String>): BatchDeletionScheduleResult {
        require(fileIds.isNotEmpty()) { "fileIds must not be empty" }
        val successful = mutableListOf<String>()
        val failed = linkedMapOf<String, String>()
        for (fileId in fileIds.distinct()) {
            when (val result = schedule(fileId)) {
                is DeletionScheduleResult.Scheduled -> successful += fileId
                is DeletionScheduleResult.Rejected -> failed[fileId] = result.reason
            }
        }
        return BatchDeletionScheduleResult(successful, failed)
    }

    private suspend fun markScheduled(fileId: String): DeletionScheduleResult = try {
        val record = dao.getRecord(fileId)
        when {
            record == null -> DeletionScheduleResult.Rejected(fileId, "Media file not found")
            record.state == MediaState.DELETE -> DeletionScheduleResult.Scheduled(
                fileId = fileId,
                scheduledAt = record.deletionScheduledAt ?: record.updatedTime,
                alreadyScheduled = true
            )
            isSyncActive(fileId) || record.state in MediaState.ACTIVE_SYNC_STATES ->
                DeletionScheduleResult.Rejected(fileId, "Media file is still being synchronized")
            record.state !in MediaState.DELETABLE_STATES ->
                DeletionScheduleResult.Rejected(fileId, "Media file cannot be deleted in state ${record.state}")
            else -> {
                val now = clock.now()
                val updated = dao.markScheduledForDeletion(
                    fileId = fileId,
                    scheduledAt = now.wallTimeMillis,
                    scheduledElapsedRealtime = now.elapsedRealtimeMillis,
                    scheduledBootCount = now.bootCount,
                    allowedStates = MediaState.DELETABLE_STATES.toList()
                )
                if (updated == 1) {
                    Log.d(tag, "Scheduled $fileId for deletion")
                    DeletionScheduleResult.Scheduled(fileId, now.wallTimeMillis, alreadyScheduled = false)
                } else {
                    DeletionScheduleResult.Rejected(fileId, "Media file changed while scheduling deletion; retry")
                }
            }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(tag, "Database error while scheduling $fileId for deletion", e)
        DeletionScheduleResult.Rejected(fileId, "Database error while scheduling deletion")
    }

    /**
     * Enqueues background cleanup. The database is the source of truth, so a WorkManager failure
     * does not undo an accepted request: the ping-triggered fallback still cleans the file up.
     */
    private suspend fun enqueueCleanup(result: DeletionScheduleResult.Scheduled) {
        try {
            val record = dao.getRecord(result.fileId) ?: return
            val delay = eligibilityChecker.remainingMillis(record, clock.now()) ?: return
            cleanupScheduler.scheduleCleanup(result.fileId, delay)
            cleanupScheduler.ensurePeriodicSweep()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(tag, "Failed to enqueue background cleanup for ${result.fileId}; ping fallback will retry", e)
        }
    }
}
