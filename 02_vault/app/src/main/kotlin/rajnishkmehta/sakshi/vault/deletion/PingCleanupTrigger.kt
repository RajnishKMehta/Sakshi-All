/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.deletion

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import rajnishkmehta.sakshi.vault.AppLog as Log
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Fallback cleanup trigger invoked from the Vault ping flow.
 *
 * Each ping starts a background cleanup sweep without delaying the ping response. Pings that
 * arrive while a sweep is still running are coalesced into the running sweep.
 *
 * @param scope Scope that owns the background sweep, typically the service scope.
 * @param cleanup Performs one cleanup sweep.
 */
/**
 * Triggers a cleanup sweep in response to an IPC ping or generic lifecycle event.
 *
 * This acts as a fallback to [MediaCleanupScheduler], ensuring cleanup eventually runs even
 * if background jobs are severely delayed or disabled by the OS, provided the app is started.
 */
class PingCleanupTrigger(
    private val scope: CoroutineScope,
    private val cleanup: suspend () -> Unit
) {

    private val tag = "PingCleanupTrigger"
    private val running = AtomicBoolean(false)

    /**
     * Starts a cleanup sweep unless one is already running.
     *
     * @return `true` if a new sweep was started.
     */
    fun onPing(): Boolean {
        if (!running.compareAndSet(false, true)) return false
        val job = scope.launch {
            try {
                cleanup()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(tag, "Ping-triggered cleanup failed", e)
            }
        }
        job.invokeOnCompletion { running.set(false) }
        return true
    }
}
