/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.db

/**
 * Persisted values of [MediaRecord.state].
 *
 * Values are stored as plain strings so the database schema stays human readable and the
 * synchronization states keep the names already reported over IPC (for example in `isAVSynced`).
 */
object MediaState {
    /** A recording has been registered but no bytes have been copied yet. */
    const val INITIALIZING = "INITIALIZING"

    /** A recording is being incrementally copied into the Vault. */
    const val SYNCING = "SYNCING"

    /** Incremental synchronization of a recording is temporarily paused. */
    const val PAUSED = "PAUSED"

    /** The media file is fully copied into the Vault. */
    const val COMPLETED = "COMPLETED"

    /** Synchronization was stopped before completion. */
    const val STOPPED = "STOPPED"

    /** Synchronization failed. */
    const val FAILED = "FAILED"

    /**
     * The media file is scheduled for permanent deletion.
     *
     * Records in this state are hidden from `listMedia`, and are removed together
     * with their files once the retention period since [MediaRecord.deletionScheduledAt] elapsed.
     */
    const val DELETE = "DELETE"

    /** States in which a file is still being written by an active synchronization. */
    val ACTIVE_SYNC_STATES: Set<String> = setOf(INITIALIZING, SYNCING, PAUSED)

    /** States from which a file may be scheduled for deletion. */
    val DELETABLE_STATES: Set<String> = setOf(COMPLETED, FAILED, STOPPED)

    /** States whose files are exposed through `listMedia`. */
    val LISTABLE_STATES: Set<String> = setOf(COMPLETED, FAILED)
}
