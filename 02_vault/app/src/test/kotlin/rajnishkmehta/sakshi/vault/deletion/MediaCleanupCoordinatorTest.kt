/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.deletion

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import rajnishkmehta.sakshi.vault.db.MediaState
import java.io.File

class MediaCleanupCoordinatorTest {

    @TempDir
    lateinit var filesDir: File

    private val scheduledAt = 10_000L
    private var now = DeletionClockReading(scheduledAt + THIRTY_DAYS, elapsedRealtimeMillis = 0L, bootCount = null)

    private fun coordinator(dao: FakeMediaRecordDao, cleaner: MediaFileCleaner = MediaFileCleaner(filesDir)) =
        MediaCleanupCoordinator(dao, cleaner, DeletionEligibilityChecker(), { now })

    private fun scheduledRecord(fileId: String = "photo_00000001") =
        testRecord(fileId = fileId, state = MediaState.DELETE, deletionScheduledAt = scheduledAt)

    private fun mediaFile(fileId: String = "photo_00000001") = File(filesDir, "media/photo/$fileId.jpg")
    private fun thumbnail(fileId: String = "photo_00000001") = File(filesDir, "media/photo/thumbnail/$fileId.webp")

    private fun create(file: File) {
        file.parentFile!!.mkdirs()
        file.writeText("data")
    }

    @Test
    fun `deletes files then record once retention elapsed`() = runBlocking {
        create(mediaFile())
        create(thumbnail())
        val dao = FakeMediaRecordDao(scheduledRecord())

        val summary = coordinator(dao).cleanupEligibleRecords()

        assertEquals(CleanupSummary(deletedCount = 1), summary)
        assertFalse(mediaFile().exists())
        assertFalse(thumbnail().exists())
        assertTrue(dao.records.isEmpty())
    }

    @Test
    fun `keeps record and files before retention elapsed`() = runBlocking {
        create(mediaFile())
        now = now.copy(wallTimeMillis = scheduledAt + THIRTY_DAYS - 1)
        val dao = FakeMediaRecordDao(scheduledRecord())

        val summary = coordinator(dao).cleanupEligibleRecords()

        assertEquals(CleanupSummary(pendingCount = 1), summary)
        assertTrue(mediaFile().exists())
        assertEquals(1, dao.records.size)
    }

    @Test
    fun `never deletes unscheduled records`() = runBlocking {
        create(mediaFile())
        val dao = FakeMediaRecordDao(testRecord(state = MediaState.COMPLETED, deletionScheduledAt = scheduledAt))

        assertEquals(CleanupSummary(), coordinator(dao).cleanupEligibleRecords())
        assertTrue(mediaFile().exists())
    }

    @Test
    fun `already missing files still remove the record`() = runBlocking {
        val dao = FakeMediaRecordDao(scheduledRecord())
        assertEquals(CleanupSummary(deletedCount = 1), coordinator(dao).cleanupEligibleRecords())
        assertTrue(dao.records.isEmpty())
    }

    @Test
    fun `partial filesystem failure keeps record for retry`() = runBlocking {
        create(thumbnail())
        mediaFile().mkdirs()
        File(mediaFile(), "child").writeText("x")
        val dao = FakeMediaRecordDao(scheduledRecord())

        val summary = coordinator(dao).cleanupEligibleRecords()

        assertEquals(CleanupSummary(failedCount = 1), summary)
        assertEquals(MediaState.DELETE, dao.records.getValue("photo_00000001").state)
        assertTrue(summary.hasScheduledRecords)

        File(mediaFile(), "child").delete()
        mediaFile().delete()
        assertEquals(CleanupSummary(deletedCount = 1), coordinator(dao).cleanupEligibleRecords())
    }

    @Test
    fun `database delete failure keeps record and retry succeeds`() = runBlocking {
        create(mediaFile())
        val dao = FakeMediaRecordDao(scheduledRecord()).apply { failWrites = true }

        assertEquals(CleanupSummary(failedCount = 1), coordinator(dao).cleanupEligibleRecords())
        assertFalse(mediaFile().exists())
        assertEquals(1, dao.records.size)

        dao.failWrites = false
        assertEquals(CleanupSummary(deletedCount = 1), coordinator(dao).cleanupEligibleRecords())
        assertTrue(dao.records.isEmpty())
    }

    @Test
    fun `database query failure is reported as failure`() = runBlocking {
        val dao = FakeMediaRecordDao(scheduledRecord()).apply { failReads = true }
        assertEquals(CleanupSummary(failedCount = 1), coordinator(dao).cleanupEligibleRecords())
    }

    @Test
    fun `simultaneous worker and ping triggers delete each record once`() = runBlocking {
        val ids = (1..20).map { "photo_%08d".format(it) }
        ids.forEach { create(mediaFile(it)) }
        val dao = FakeMediaRecordDao(*ids.map { scheduledRecord(it) }.toTypedArray())
        val workerCoordinator = coordinator(dao)
        val pingCoordinator = coordinator(dao)

        val summaries = (1..4).map { index ->
            async(Dispatchers.Default) {
                (if (index % 2 == 0) workerCoordinator else pingCoordinator).cleanupEligibleRecords()
            }
        }.awaitAll()

        assertEquals(ids.size, summaries.sumOf { it.deletedCount })
        assertEquals(0, summaries.sumOf { it.failedCount })
        assertEquals(ids.size, dao.deleteCalls)
        assertTrue(dao.records.isEmpty())
    }
}
