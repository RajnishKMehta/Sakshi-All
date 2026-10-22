/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.deletion

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import rajnishkmehta.sakshi.vault.deletion.MediaCleanupWorker.CleanupWorkDecision

class MediaCleanupWorkerDecisionTest {

    @Test
    fun `successful sweep succeeds even when records are still pending`() {
        val summary = CleanupSummary(deletedCount = 2, pendingCount = 3)
        assertEquals(CleanupWorkDecision.SUCCESS, MediaCleanupWorker.decide(summary, 0, isPeriodicSweep = false))
        assertEquals(CleanupWorkDecision.SUCCESS, MediaCleanupWorker.decide(summary, 0, isPeriodicSweep = true))
    }

    @Test
    fun `failed one-time cleanup is retried while attempts remain`() {
        val summary = CleanupSummary(failedCount = 1)
        (0 until MediaCleanupWorker.MAX_RUN_ATTEMPTS - 1).forEach { attempt ->
            assertEquals(CleanupWorkDecision.RETRY, MediaCleanupWorker.decide(summary, attempt, isPeriodicSweep = false))
        }
    }

    @Test
    fun `failed one-time cleanup gives up after max attempts`() {
        assertEquals(
            CleanupWorkDecision.FAILURE,
            MediaCleanupWorker.decide(CleanupSummary(failedCount = 1), MediaCleanupWorker.MAX_RUN_ATTEMPTS - 1, isPeriodicSweep = false)
        )
    }

    @Test
    fun `failed periodic sweep succeeds so it stays scheduled`() {
        val summary = CleanupSummary(failedCount = 1)
        listOf(0, MediaCleanupWorker.MAX_RUN_ATTEMPTS - 1, MediaCleanupWorker.MAX_RUN_ATTEMPTS + 10).forEach { attempt ->
            assertEquals(CleanupWorkDecision.SUCCESS, MediaCleanupWorker.decide(summary, attempt, isPeriodicSweep = true))
        }
    }
}
