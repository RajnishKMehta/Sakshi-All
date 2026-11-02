/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.sync

import android.content.Context
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import rajnishkmehta.sakshi.vault.db.MediaState
import rajnishkmehta.sakshi.vault.db.VaultDatabase
import rajnishkmehta.sakshi.vault.deletion.DeletionClockReading
import rajnishkmehta.sakshi.vault.deletion.DeletionEligibilityChecker
import rajnishkmehta.sakshi.vault.deletion.DeletionScheduleResult
import rajnishkmehta.sakshi.vault.deletion.FakeCleanupScheduler
import rajnishkmehta.sakshi.vault.deletion.FakeMediaRecordDao
import rajnishkmehta.sakshi.vault.deletion.MediaDeletionScheduler
import rajnishkmehta.sakshi.vault.deletion.testRecord
import rajnishkmehta.sakshi.vault.storage.StorageManager
import java.io.ByteArrayInputStream
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class CopyEngineDeletionLockTest {

    @TempDir
    lateinit var tempDir: File

    private val fileId = "photo_lock_0001"
    private val saveStarted = CountDownLatch(1)
    private val releaseSave = CountDownLatch(1)
    private val storageManager = mockk<StorageManager>()
    private val context = mockk<Context>()

    @BeforeEach
    fun setUp() {
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk()
        every { context.contentResolver.openInputStream(any()) } answers { ByteArrayInputStream(ByteArray(8)) }
        every { storageManager.saveMedia(fileId, any(), "PHOTO", "jpg") } answers {
            saveStarted.countDown()
            check(releaseSave.await(5, TimeUnit.SECONDS)) { "save was never released" }
            File(tempDir, "$fileId.jpg").apply { writeBytes(ByteArray(8)) }.absolutePath
        }
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Uri::class)
    }

    private fun copyEngine(dao: FakeMediaRecordDao): CopyEngine {
        val database = mockk<VaultDatabase> { every { mediaRecordDao() } returns dao }
        return CopyEngine(context, database, storageManager)
    }

    private fun deletionScheduler(dao: FakeMediaRecordDao) = MediaDeletionScheduler(
        dao = dao,
        clock = { DeletionClockReading(wallTimeMillis = 9_000L, elapsedRealtimeMillis = 1L, bootCount = 1) },
        eligibilityChecker = DeletionEligibilityChecker(),
        cleanupScheduler = FakeCleanupScheduler(),
        isSyncActive = { false }
    )

    @Test
    fun `copy retry fails with DuplicateFileId exception for existing completed record`() = runBlocking {
        val dao = FakeMediaRecordDao(testRecord(fileId = fileId, state = MediaState.COMPLETED))
        val exception = assertThrows<IllegalArgumentException> {
            runBlocking { copyEngine(dao).copyFile(fileId, "content://camera/$fileId", "PHOTO", "jpg") }
        }
        assertTrue(exception.message?.startsWith("Duplicate file ID") == true)
    }

    @Test
    fun `copy retry never overwrites a record scheduled for deletion`() = runBlocking {
        val scheduled = testRecord(fileId = fileId, state = MediaState.DELETE, deletionScheduledAt = 1L)
        val dao = FakeMediaRecordDao(scheduled)

        assertThrows<IllegalArgumentException> {
            runBlocking { copyEngine(dao).copyFile(fileId, "content://camera/$fileId", "PHOTO", "jpg") }
        }

        assertEquals(scheduled, dao.records.getValue(fileId))
        verify(exactly = 0) { storageManager.saveMedia(any(), any(), any(), any()) }
    }
}
