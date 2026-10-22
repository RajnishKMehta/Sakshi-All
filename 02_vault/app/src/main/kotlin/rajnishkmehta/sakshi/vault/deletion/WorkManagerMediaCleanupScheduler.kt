/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.deletion

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

/**
 * [MediaCleanupScheduler] backed by Jetpack WorkManager.
 *
 * WorkManager persists requests in its own database, so cleanup survives process death, Vault
 * service restarts, and device reboots without relying on in-memory timers.
 *
 * - One unique one-time request per file (`ExistingWorkPolicy.KEEP`) fires when the file's retention
 *   period is expected to end. Duplicate scheduling requests never reset the pending delay.
 * - One unique periodic sweep (`ExistingPeriodicWorkPolicy.KEEP`) retries failed or delayed cleanups.
 * - Both use exponential backoff and only require a non-low battery, since cleanup is not urgent
 *   and frees storage.
 */
class WorkManagerMediaCleanupScheduler(context: Context) : MediaCleanupScheduler {

    private val workManager = WorkManager.getInstance(context.applicationContext)

    private val constraints = Constraints.Builder()
        .setRequiresBatteryNotLow(true)
        .build()

    override fun scheduleCleanup(fileId: String, delayMillis: Long) {
        val request = OneTimeWorkRequestBuilder<MediaCleanupWorker>()
            .setInitialDelay(delayMillis.coerceAtLeast(0L), TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_DELAY_MINUTES, TimeUnit.MINUTES)
            .setInputData(workDataOf(MediaCleanupWorker.KEY_FILE_ID to fileId))
            .addTag(WORK_TAG)
            .build()
        workManager.enqueueUniqueWork(fileWorkName(fileId), ExistingWorkPolicy.KEEP, request)
    }

    override fun ensurePeriodicSweep() {
        val request = PeriodicWorkRequestBuilder<MediaCleanupWorker>(SWEEP_INTERVAL_HOURS, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInputData(workDataOf(MediaCleanupWorker.KEY_PERIODIC_SWEEP to true))
            .addTag(WORK_TAG)
            .build()
        workManager.enqueueUniquePeriodicWork(SWEEP_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    companion object {
        /** Tag shared by all media cleanup work requests. */
        const val WORK_TAG = "scheduled-media-cleanup"

        /** Unique name of the periodic safety-net sweep. */
        const val SWEEP_WORK_NAME = "scheduled-media-cleanup-sweep"

        private const val SWEEP_INTERVAL_HOURS = 24L
        private const val BACKOFF_DELAY_MINUTES = 15L

        /** Unique name of the one-time cleanup request for [fileId]. */
        fun fileWorkName(fileId: String): String = "scheduled-media-cleanup-$fileId"
    }
}
