/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.sync

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Process-wide registry of per-file locks.
 *
 * Synchronization passes, deletion scheduling, and permanent cleanup all acquire the lock of the
 * file they modify, so a file can never be copied and deleted at the same time. The registry is a
 * singleton because the IPC service and WorkManager workers are created independently in the same
 * Vault process.
 */
/**
 * Provides fine-grained locking mechanism for concurrent operations on individual media files.
 *
 * Prevents race conditions between synchronization, deletion, recovery, and cleanup tasks
 * by enforcing mutually exclusive access to the file state based on its `fileId`.
 */
object MediaFileLocks {

    private val locks = ConcurrentHashMap<String, Mutex>()

    /** Returns the lock guarding all mutations of [fileId]. */
    fun lockFor(fileId: String): Mutex = locks.getOrPut(fileId) { Mutex() }

    /** Runs [action] while holding the lock of [fileId]. */
    suspend fun <T> withFileLock(fileId: String, action: suspend () -> T): T =
        lockFor(fileId).withLock { action() }
}
