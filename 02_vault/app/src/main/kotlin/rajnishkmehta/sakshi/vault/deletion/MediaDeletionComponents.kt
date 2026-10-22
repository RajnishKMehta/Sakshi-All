/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.deletion

import android.content.Context
import rajnishkmehta.sakshi.vault.db.VaultDatabase

/**
 * Wires the production deletion components for the Vault service and WorkManager workers.
 */
class MediaDeletionComponents private constructor(
    /** Coordinator that permanently deletes eligible media. */
    val cleanupCoordinator: MediaCleanupCoordinator,
    /** Scheduler that enqueues persistent cleanup work. */
    val cleanupScheduler: MediaCleanupScheduler,
    private val database: VaultDatabase,
    private val clock: DeletionClock,
    private val eligibilityChecker: DeletionEligibilityChecker
) {

    /** Creates a [MediaDeletionScheduler] that rejects files for which [isSyncActive] returns `true`. */
    fun deletionScheduler(isSyncActive: (String) -> Boolean): MediaDeletionScheduler =
        MediaDeletionScheduler(
            dao = database.mediaRecordDao(),
            clock = clock,
            eligibilityChecker = eligibilityChecker,
            cleanupScheduler = cleanupScheduler,
            isSyncActive = isSyncActive
        )

    companion object {
        /** Builds the production components for [context]. */
        fun from(context: Context): MediaDeletionComponents {
            val appContext = context.applicationContext
            val database = VaultDatabase.getDatabase(appContext)
            val clock = SystemDeletionClock(appContext)
            val eligibilityChecker = DeletionEligibilityChecker()
            return MediaDeletionComponents(
                cleanupCoordinator = MediaCleanupCoordinator(
                    dao = database.mediaRecordDao(),
                    fileCleaner = MediaFileCleaner(appContext.filesDir),
                    eligibilityChecker = eligibilityChecker,
                    clock = clock
                ),
                cleanupScheduler = WorkManagerMediaCleanupScheduler(appContext),
                database = database,
                clock = clock,
                eligibilityChecker = eligibilityChecker
            )
        }
    }
}
