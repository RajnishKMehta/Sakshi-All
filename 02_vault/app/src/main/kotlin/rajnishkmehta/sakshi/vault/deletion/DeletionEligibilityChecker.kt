/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.deletion

import rajnishkmehta.sakshi.vault.db.MediaRecord
import rajnishkmehta.sakshi.vault.db.MediaState
import java.util.concurrent.TimeUnit

/**
 * Decides whether a media record scheduled for deletion may be permanently removed.
 *
 * A record is eligible only if it is in [MediaState.DELETE], has a scheduling timestamp, and at
 * least [retentionMillis] (30 full days by default) have elapsed since it was scheduled.
 *
 * Clock handling:
 * - If the device has not rebooted since scheduling (same boot count), the monotonic
 *   `elapsedRealtime` clock is used. Wall-clock changes cannot shorten or extend retention.
 * - After a reboot the monotonic clock is reset, so only the wall clock is available. Moving the
 *   wall clock backwards delays deletion; moving it forwards on an offline device can shorten the
 *   effective retention. This limitation cannot be eliminated without a trusted time source.
 */
class DeletionEligibilityChecker(
    private val retentionMillis: Long = DEFAULT_RETENTION_MILLIS
) {

    init {
        require(retentionMillis > 0) { "retentionMillis must be positive" }
    }

    /** Returns `true` if [record] may be permanently deleted at [now]. */
    fun isEligible(record: MediaRecord, now: DeletionClockReading): Boolean =
        remainingMillis(record, now) == 0L

    /**
     * Returns the time left until [record] becomes eligible at [now], `0` if it is already
     * eligible, or `null` if the record is not scheduled for deletion.
     */
    fun remainingMillis(record: MediaRecord, now: DeletionClockReading): Long? {
        if (record.state != MediaState.DELETE) return null
        val scheduledAt = record.deletionScheduledAt ?: return null

        val elapsedSinceScheduling = monotonicElapsedMillis(record, now)
            ?: (now.wallTimeMillis - scheduledAt)

        return (retentionMillis - elapsedSinceScheduling).coerceIn(0L, Long.MAX_VALUE)
    }

    private fun monotonicElapsedMillis(record: MediaRecord, now: DeletionClockReading): Long? {
        val scheduledBootCount = record.deletionScheduledBootCount ?: return null
        val scheduledElapsed = record.deletionScheduledElapsedRealtime ?: return null
        if (now.bootCount == null || now.bootCount != scheduledBootCount) return null
        if (now.elapsedRealtimeMillis < scheduledElapsed) return null
        return now.elapsedRealtimeMillis - scheduledElapsed
    }

    companion object {
        /** Default retention period before scheduled media is permanently deleted: 30 days. */
        val DEFAULT_RETENTION_MILLIS: Long = TimeUnit.DAYS.toMillis(30)
    }
}
