/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.deletion

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import rajnishkmehta.sakshi.vault.db.MediaState

class DeletionEligibilityCheckerTest {

    private val checker = DeletionEligibilityChecker()
    private val scheduledAt = 1_700_000_000_000L

    private fun scheduled(bootCount: Int? = null, elapsed: Long? = null) = testRecord(
        state = MediaState.DELETE,
        deletionScheduledAt = scheduledAt,
        deletionScheduledElapsedRealtime = elapsed,
        deletionScheduledBootCount = bootCount
    )

    private fun reading(wall: Long, elapsed: Long = 0L, bootCount: Int? = null) =
        DeletionClockReading(wall, elapsed, bootCount)

    @Test
    fun `default retention is thirty days`() {
        assertEquals(THIRTY_DAYS, DeletionEligibilityChecker.DEFAULT_RETENTION_MILLIS)
    }

    @Test
    fun `not eligible one millisecond before thirty full days`() {
        assertFalse(checker.isEligible(scheduled(), reading(scheduledAt + THIRTY_DAYS - 1)))
        assertEquals(1L, checker.remainingMillis(scheduled(), reading(scheduledAt + THIRTY_DAYS - 1)))
    }

    @Test
    fun `eligible exactly at thirty full days`() {
        assertTrue(checker.isEligible(scheduled(), reading(scheduledAt + THIRTY_DAYS)))
        assertEquals(0L, checker.remainingMillis(scheduled(), reading(scheduledAt + THIRTY_DAYS)))
    }

    @Test
    fun `unscheduled records are never eligible`() {
        val now = reading(scheduledAt + 10 * THIRTY_DAYS)
        listOf(MediaState.COMPLETED, MediaState.FAILED, MediaState.STOPPED, MediaState.SYNCING).forEach { state ->
            val record = testRecord(state = state, deletionScheduledAt = scheduledAt)
            assertFalse(checker.isEligible(record, now))
            assertNull(checker.remainingMillis(record, now))
        }
    }

    @Test
    fun `delete record without timestamp is never eligible`() {
        val record = testRecord(state = MediaState.DELETE, deletionScheduledAt = null)
        assertFalse(checker.isEligible(record, reading(Long.MAX_VALUE / 2)))
    }

    @Test
    fun `wall clock moved backwards delays deletion after reboot`() {
        assertEquals(THIRTY_DAYS + 5_000L, checker.remainingMillis(scheduled(), reading(scheduledAt - 5_000L)))
    }

    @Test
    fun `same boot uses monotonic clock so a forward wall clock jump cannot delete early`() {
        val record = scheduled(bootCount = 7, elapsed = 1_000L)
        val now = reading(wall = scheduledAt + 365 * THIRTY_DAYS, elapsed = 1_000L + THIRTY_DAYS - 1, bootCount = 7)
        assertFalse(checker.isEligible(record, now))
        assertTrue(checker.isEligible(record, now.copy(elapsedRealtimeMillis = 1_000L + THIRTY_DAYS)))
    }

    @Test
    fun `same boot uses monotonic clock when wall clock moved backwards`() {
        val record = scheduled(bootCount = 7, elapsed = 1_000L)
        val now = reading(wall = scheduledAt - THIRTY_DAYS, elapsed = 1_000L + THIRTY_DAYS, bootCount = 7)
        assertTrue(checker.isEligible(record, now))
    }

    @Test
    fun `different boot falls back to wall clock`() {
        val record = scheduled(bootCount = 7, elapsed = 1_000L)
        val now = reading(wall = scheduledAt + THIRTY_DAYS, elapsed = 10L, bootCount = 8)
        assertTrue(checker.isEligible(record, now))
        assertFalse(checker.isEligible(record, now.copy(wallTimeMillis = scheduledAt + THIRTY_DAYS - 1)))
    }
}
