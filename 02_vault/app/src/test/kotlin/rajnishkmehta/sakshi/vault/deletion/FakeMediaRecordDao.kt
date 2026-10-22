/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.deletion

import rajnishkmehta.sakshi.vault.db.MediaRecord
import rajnishkmehta.sakshi.vault.db.MediaRecordDao
import rajnishkmehta.sakshi.vault.db.MediaState
import java.util.concurrent.ConcurrentHashMap

/** In-memory [MediaRecordDao] mirroring the SQL semantics of the Room queries. */
internal class FakeMediaRecordDao(vararg initial: MediaRecord) : MediaRecordDao {

    val records = ConcurrentHashMap<String, MediaRecord>().apply { initial.forEach { put(it.fileId, it) } }
    var failReads = false
    var failWrites = false
    var deleteCalls = 0

    private fun checkRead() = check(!failReads) { "simulated database read failure" }
    private fun checkWrite() = check(!failWrites) { "simulated database write failure" }

    override suspend fun insertRecord(record: MediaRecord) { checkWrite(); records[record.fileId] = record }
    override suspend fun updateRecord(record: MediaRecord) { checkWrite(); records.replace(record.fileId, record) }
    override suspend fun getRecord(fileId: String): MediaRecord? { checkRead(); return records[fileId] }
    override suspend fun getAllRecords(): List<MediaRecord> { checkRead(); return records.values.sortedByDescending { it.createdTime } }
    override suspend fun getRecordsInState(state: String): List<MediaRecord> { checkRead(); return records.values.filter { it.state == state } }
    override suspend fun deleteRecord(fileId: String) { checkWrite(); records.remove(fileId) }

    override suspend fun markScheduledForDeletion(
        fileId: String,
        scheduledAt: Long,
        scheduledElapsedRealtime: Long,
        scheduledBootCount: Int?,
        allowedStates: List<String>
    ): Int {
        checkWrite()
        val record = records[fileId]?.takeIf { it.state in allowedStates } ?: return 0
        records[fileId] = record.copy(
            state = MediaState.DELETE,
            deletionScheduledAt = scheduledAt,
            deletionScheduledElapsedRealtime = scheduledElapsedRealtime,
            deletionScheduledBootCount = scheduledBootCount,
            updatedTime = scheduledAt
        )
        return 1
    }

    override suspend fun deleteScheduledRecord(fileId: String, scheduledAt: Long): Int {
        checkWrite()
        deleteCalls++
        val record = records[fileId] ?: return 0
        if (record.state != MediaState.DELETE || record.deletionScheduledAt != scheduledAt) return 0
        records.remove(fileId)
        return 1
    }
}

/** Records cleanup requests instead of using WorkManager. */
internal class FakeCleanupScheduler : MediaCleanupScheduler {
    val scheduled = mutableListOf<Pair<String, Long>>()
    var sweepEnsured = 0
    var failOnSchedule = false

    override fun scheduleCleanup(fileId: String, delayMillis: Long) {
        check(!failOnSchedule) { "simulated WorkManager failure" }
        scheduled += fileId to delayMillis
    }

    override fun ensurePeriodicSweep() { sweepEnsured++ }
}
