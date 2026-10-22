/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.deletion

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rajnishkmehta.sakshi.vault.db.MediaState

class MediaDeletionSchedulerTest {

    private val now = DeletionClockReading(wallTimeMillis = 5_000_000L, elapsedRealtimeMillis = 42L, bootCount = 3)
    private val cleanupScheduler = FakeCleanupScheduler()
    private val activeSyncs = mutableSetOf<String>()

    private fun scheduler(dao: FakeMediaRecordDao) = MediaDeletionScheduler(
        dao = dao,
        clock = { now },
        eligibilityChecker = DeletionEligibilityChecker(),
        cleanupScheduler = cleanupScheduler,
        isSyncActive = { it in activeSyncs }
    )

    @Test
    fun `scheduling marks record as DELETE without touching files and enqueues cleanup`() = runBlocking {
        val dao = FakeMediaRecordDao(testRecord(fileId = "file_0001"))

        val result = scheduler(dao).schedule("file_0001")

        assertEquals(DeletionScheduleResult.Scheduled("file_0001", now.wallTimeMillis, alreadyScheduled = false), result)
        val record = dao.records.getValue("file_0001")
        assertEquals(MediaState.DELETE, record.state)
        assertEquals(now.wallTimeMillis, record.deletionScheduledAt)
        assertEquals(now.elapsedRealtimeMillis, record.deletionScheduledElapsedRealtime)
        assertEquals(now.bootCount, record.deletionScheduledBootCount)
        assertEquals(listOf("file_0001" to THIRTY_DAYS), cleanupScheduler.scheduled)
        assertEquals(1, cleanupScheduler.sweepEnsured)
    }

    @Test
    fun `failed and stopped records can be scheduled`() = runBlocking {
        val dao = FakeMediaRecordDao(
            testRecord(fileId = "file_fail", state = MediaState.FAILED),
            testRecord(fileId = "file_stop", state = MediaState.STOPPED)
        )
        val scheduler = scheduler(dao)
        assertTrue(scheduler.schedule("file_fail") is DeletionScheduleResult.Scheduled)
        assertTrue(scheduler.schedule("file_stop") is DeletionScheduleResult.Scheduled)
    }

    @Test
    fun `repeated request is idempotent and keeps original timestamp`() = runBlocking {
        val original = testRecord(fileId = "file_0001", state = MediaState.DELETE, deletionScheduledAt = 1_000L)
        val dao = FakeMediaRecordDao(original)

        val result = scheduler(dao).schedule("file_0001")

        assertEquals(DeletionScheduleResult.Scheduled("file_0001", 1_000L, alreadyScheduled = true), result)
        assertEquals(original, dao.records.getValue("file_0001"))
    }

    @Test
    fun `missing record is rejected`() = runBlocking {
        val result = scheduler(FakeMediaRecordDao()).schedule("file_missing")
        assertEquals(DeletionScheduleResult.Rejected("file_missing", "Media file not found"), result)
        assertTrue(cleanupScheduler.scheduled.isEmpty())
    }

    @Test
    fun `invalid file id is rejected before database access`() = runBlocking {
        val dao = FakeMediaRecordDao().apply { failReads = true }
        listOf("", "short", "../../etc/passwd", "UPPERCASE_ID").forEach { id ->
            val result = scheduler(dao).schedule(id)
            assertTrue(result is DeletionScheduleResult.Rejected, "Expected rejection for '$id'")
        }
    }

    @Test
    fun `active synchronization is rejected and record unchanged`() = runBlocking {
        val syncing = testRecord(fileId = "video_0001", state = MediaState.SYNCING, mediaType = "VIDEO", fileExtension = "mp4")
        val completedButFinalizing = testRecord(fileId = "video_0002", mediaType = "VIDEO", fileExtension = "mp4")
        activeSyncs += "video_0002"
        val dao = FakeMediaRecordDao(syncing, completedButFinalizing)

        val scheduler = scheduler(dao)
        listOf("video_0001", "video_0002").forEach { id ->
            assertEquals(
                DeletionScheduleResult.Rejected(id, "Media file is still being synchronized"),
                scheduler.schedule(id)
            )
        }
        assertEquals(syncing, dao.records.getValue("video_0001"))
        assertEquals(completedButFinalizing, dao.records.getValue("video_0002"))
    }

    @Test
    fun `paused and initializing records are rejected`() = runBlocking {
        val dao = FakeMediaRecordDao(
            testRecord(fileId = "video_paused", state = MediaState.PAUSED),
            testRecord(fileId = "video_initial", state = MediaState.INITIALIZING)
        )
        val scheduler = scheduler(dao)
        assertTrue(scheduler.schedule("video_paused") is DeletionScheduleResult.Rejected)
        assertTrue(scheduler.schedule("video_initial") is DeletionScheduleResult.Rejected)
    }

    @Test
    fun `database error is reported as rejection`() = runBlocking {
        val dao = FakeMediaRecordDao(testRecord(fileId = "file_0001")).apply { failWrites = true }
        assertEquals(
            DeletionScheduleResult.Rejected("file_0001", "Database error while scheduling deletion"),
            scheduler(dao).schedule("file_0001")
        )
        assertEquals(MediaState.COMPLETED, dao.records.getValue("file_0001").state)
    }

    @Test
    fun `work manager failure does not undo accepted request`() = runBlocking {
        cleanupScheduler.failOnSchedule = true
        val dao = FakeMediaRecordDao(testRecord(fileId = "file_0001"))
        assertTrue(scheduler(dao).schedule("file_0001") is DeletionScheduleResult.Scheduled)
        assertEquals(MediaState.DELETE, dao.records.getValue("file_0001").state)
    }

    @Test
    fun `batch reports partial failures and collapses duplicates`() = runBlocking {
        val dao = FakeMediaRecordDao(
            testRecord(fileId = "file_0001"),
            testRecord(fileId = "file_0002", state = MediaState.DELETE, deletionScheduledAt = 1L),
            testRecord(fileId = "video_0003", state = MediaState.SYNCING)
        )

        val result = scheduler(dao).scheduleBatch(
            listOf("file_0001", "file_0002", "file_0001", "video_0003", "file_missing", "bad id")
        )

        assertEquals(listOf("file_0001", "file_0002"), result.successfulIds)
        assertEquals(setOf("video_0003", "file_missing", "bad id"), result.failedIds.keys)
        assertEquals("Media file is still being synchronized", result.failedIds["video_0003"])
        assertEquals("Media file not found", result.failedIds["file_missing"])
    }

    @Test
    fun `empty batch is invalid`() {
        assertThrows<IllegalArgumentException> {
            runBlocking { scheduler(FakeMediaRecordDao()).scheduleBatch(emptyList()) }
        }
    }
}
