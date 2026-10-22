/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.deletion

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

class PingCleanupTriggerTest {

    @Test
    fun `duplicate pings while a sweep runs are coalesced`() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val finished = CompletableDeferred<Unit>()
        val runs = AtomicInteger()
        val trigger = PingCleanupTrigger(scope) {
            runs.incrementAndGet()
            started.complete(Unit)
            release.await()
            finished.complete(Unit)
        }

        assertTrue(trigger.onPing())
        withTimeout(5_000) { started.await() }
        assertFalse(trigger.onPing())
        release.complete(Unit)
        withTimeout(5_000) { finished.await() }

        withTimeout(5_000) { while (!trigger.onPing()) kotlinx.coroutines.delay(10) }
        assertTrue(runs.get() >= 1)
        scope.cancel()
    }

    @Test
    fun `cleanup failure does not crash and allows the next ping`() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val runs = AtomicInteger()
        val trigger = PingCleanupTrigger(scope) {
            runs.incrementAndGet()
            throw IllegalStateException("disk error")
        }

        assertTrue(trigger.onPing())
        withTimeout(5_000) { while (!trigger.onPing()) kotlinx.coroutines.delay(10) }
        withTimeout(5_000) { while (runs.get() < 2) kotlinx.coroutines.delay(10) }
        assertEquals(true, scope.coroutineContext[kotlinx.coroutines.Job]!!.isActive)
        scope.cancel()
    }
}
