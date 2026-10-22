/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.deletion

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rajnishkmehta.sakshi.vault.AppLog as Log
import rajnishkmehta.sakshi.vault.db.MediaRecordDao
import rajnishkmehta.sakshi.vault.db.MediaState
import rajnishkmehta.sakshi.vault.sync.MediaFileLocks

/**
 * Summary of a cleanup sweep.
 *
 * @property deletedCount Records whose files and database row were permanently removed.
 * @property failedCount Eligible records whose cleanup failed and must be retried.
 * @property pendingCount Scheduled records that have not reached the retention threshold yet.
 */
data class CleanupSummary(
    val deletedCount: Int = 0,
    val failedCount: Int = 0,
    val pendingCount: Int = 0
) {
    /** `true` if records in [MediaState.DELETE] remain after the sweep. */
    val hasScheduledRecords: Boolean get() = failedCount > 0 || pendingCount > 0
}

/**
 * Coordinates permanent cleanup of media scheduled for deletion. Used by both the WorkManager
 * worker and the ping-triggered fallback.
 *
 * Safety guarantees:
 * - Sweeps are serialized process-wide, so simultaneous WorkManager and ping triggers never clean
 *   the same record concurrently.
 * - Each record is re-read and re-checked under its [MediaFileLocks] lock right before cleanup, so
 *   synchronization never writes a file that is being deleted.
 * - Files are deleted first. The database record is removed only after all Vault-private files are
 *   gone, and only if it is still the same scheduled record. On partial failure the record stays in
 *   [MediaState.DELETE] and the next trigger retries; already-missing files are treated as deleted.
 */
class MediaCleanupCoordinator(
    private val dao: MediaRecordDao,
    private val fileCleaner: MediaFileCleaner,
    private val eligibilityChecker: DeletionEligibilityChecker,
    private val clock: DeletionClock
) {

    private val tag = "MediaCleanupCoordinator"

    /**
     * Permanently deletes every record in [MediaState.DELETE] whose retention period elapsed.
     * Never throws for per-record failures; they are reported through [CleanupSummary.failedCount].
     */
    suspend fun cleanupEligibleRecords(): CleanupSummary = sweepMutex.withLock {
        val now = clock.now()
        val scheduledRecords = try {
            dao.getRecordsInState(MediaState.DELETE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(tag, "Failed to query records scheduled for deletion", e)
            return@withLock CleanupSummary(failedCount = 1)
        }

        var deleted = 0
        var failed = 0
        var pending = 0
        for (record in scheduledRecords) {
            if (!eligibilityChecker.isEligible(record, now)) {
                pending++
                continue
            }
            when (cleanupRecord(record.fileId, now)) {
                RecordOutcome.DELETED -> deleted++
                RecordOutcome.FAILED -> failed++
                RecordOutcome.NOT_ELIGIBLE -> pending++
                RecordOutcome.SKIPPED -> Unit
            }
        }

        if (scheduledRecords.isNotEmpty()) {
            Log.d(tag, "Cleanup sweep finished: deleted=$deleted, failed=$failed, pending=$pending")
        }
        CleanupSummary(deletedCount = deleted, failedCount = failed, pendingCount = pending)
    }

    private suspend fun cleanupRecord(fileId: String, now: DeletionClockReading): RecordOutcome =
        MediaFileLocks.withFileLock(fileId) {
            try {
                val record = dao.getRecord(fileId)
                when {
                    record == null || record.state != MediaState.DELETE -> RecordOutcome.SKIPPED
                    !eligibilityChecker.isEligible(record, now) -> RecordOutcome.NOT_ELIGIBLE
                    !fileCleaner.deleteFiles(record) -> {
                        Log.e(tag, "Cleanup of $fileId incomplete; record kept for retry")
                        RecordOutcome.FAILED
                    }
                    else -> {
                        val scheduledAt = checkNotNull(record.deletionScheduledAt)
                        if (dao.deleteScheduledRecord(fileId, scheduledAt) == 1) {
                            Log.d(tag, "Permanently deleted scheduled media $fileId")
                            RecordOutcome.DELETED
                        } else {
                            RecordOutcome.SKIPPED
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(tag, "Cleanup of $fileId failed; record kept for retry", e)
                RecordOutcome.FAILED
            }
        }

    private enum class RecordOutcome { DELETED, FAILED, NOT_ELIGIBLE, SKIPPED }

    private companion object {
        /** Shared by all coordinator instances in the process (service and workers). */
        val sweepMutex = Mutex()
    }
}
