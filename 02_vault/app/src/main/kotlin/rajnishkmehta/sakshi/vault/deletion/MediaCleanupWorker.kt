/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.deletion

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import rajnishkmehta.sakshi.vault.AppLog as Log

/**
 * WorkManager worker that permanently deletes all media whose deletion retention period elapsed.
 *
 * Every run performs a full sweep through [MediaCleanupCoordinator], so one-time per-file requests
 * and the periodic sweep share the same idempotent logic. Failed cleanups are retried with the
 * request's backoff policy up to [MAX_RUN_ATTEMPTS] times; after that the periodic sweep and the
 * ping-triggered fallback keep retrying.
 */
class MediaCleanupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val requestedFileId = inputData.getString(KEY_FILE_ID)
        Log.d(TAG, "Running scheduled media cleanup (fileId=${requestedFileId ?: "sweep"}, attempt=$runAttemptCount)")
        val decision = try {
            val components = MediaDeletionComponents.from(applicationContext)
            val summary = components.cleanupCoordinator.cleanupEligibleRecords()
            if (summary.hasScheduledRecords) {
                components.cleanupScheduler.ensurePeriodicSweep()
            }
            decide(summary, runAttemptCount)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Scheduled media cleanup failed unexpectedly", e)
            retryOrFail(runAttemptCount)
        }
        return when (decision) {
            CleanupWorkDecision.SUCCESS -> Result.success()
            CleanupWorkDecision.RETRY -> Result.retry()
            CleanupWorkDecision.FAILURE -> Result.failure()
        }
    }

    /** Outcome of a cleanup run, independent of WorkManager types for testability. */
    enum class CleanupWorkDecision { SUCCESS, RETRY, FAILURE }

    companion object {
        /** Input key holding the identifier of the file that triggered a one-time request. */
        const val KEY_FILE_ID = "file_id"

        /** Maximum number of attempts before a failing run gives up and leaves retries to the sweep. */
        const val MAX_RUN_ATTEMPTS = 5

        private const val TAG = "MediaCleanupWorker"

        /**
         * Maps a cleanup [summary] to a work result. Runs with failed cleanups are retried while
         * fewer than [MAX_RUN_ATTEMPTS] attempts were made.
         *
         * @param runAttemptCount WorkManager's zero-based attempt counter.
         */
        fun decide(summary: CleanupSummary, runAttemptCount: Int): CleanupWorkDecision =
            if (summary.failedCount > 0) retryOrFail(runAttemptCount) else CleanupWorkDecision.SUCCESS

        private fun retryOrFail(runAttemptCount: Int): CleanupWorkDecision =
            if (runAttemptCount + 1 < MAX_RUN_ATTEMPTS) CleanupWorkDecision.RETRY else CleanupWorkDecision.FAILURE
    }
}
