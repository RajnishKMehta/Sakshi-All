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
        assertEquals(CleanupWorkDecision.SUCCESS, MediaCleanupWorker.decide(CleanupSummary(deletedCount = 2, pendingCount = 3), 0))
    }

    @Test
    fun `failed cleanup is retried while attempts remain`() {
        val summary = CleanupSummary(failedCount = 1)
        (0 until MediaCleanupWorker.MAX_RUN_ATTEMPTS - 1).forEach { attempt ->
            assertEquals(CleanupWorkDecision.RETRY, MediaCleanupWorker.decide(summary, attempt))
        }
    }

    @Test
    fun `failed cleanup gives up after max attempts`() {
        assertEquals(
            CleanupWorkDecision.FAILURE,
            MediaCleanupWorker.decide(CleanupSummary(failedCount = 1), MediaCleanupWorker.MAX_RUN_ATTEMPTS - 1)
        )
    }
}
