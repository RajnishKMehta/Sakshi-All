/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.deletion

/**
 * Schedules persistent background cleanup of media files that are scheduled for deletion.
 */
interface MediaCleanupScheduler {

    /**
     * Ensures a one-time cleanup runs for [fileId] after at least [delayMillis].
     * Repeated calls for the same file keep the already pending work.
     */
    fun scheduleCleanup(fileId: String, delayMillis: Long)

    /**
     * Ensures the periodic safety-net sweep is enqueued. It retries cleanups that failed or were
     * delayed by clock changes. Repeated calls keep the existing periodic work.
     */
    fun ensurePeriodicSweep()
}
