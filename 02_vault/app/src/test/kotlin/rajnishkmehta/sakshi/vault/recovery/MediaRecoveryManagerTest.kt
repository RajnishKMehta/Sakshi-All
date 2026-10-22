package rajnishkmehta.sakshi.vault.recovery

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rajnishkmehta.sakshi.vault.db.MediaRecord
import rajnishkmehta.sakshi.vault.db.MediaState
import rajnishkmehta.sakshi.vault.deletion.FakeMediaRecordDao

class MediaRecoveryManagerTest {

    private lateinit var classUnderTest: MediaRecoveryManager
    private lateinit var dao: FakeMediaRecordDao

    @BeforeEach
    fun setUp() {
        dao = FakeMediaRecordDao()
        classUnderTest = MediaRecoveryManager(dao)
    }

    @Test
    fun `recover successful when file is in DELETE state`() = runBlocking {
        val record = MediaRecord(
            fileId = "video_2026_01",
            originalUri = "content://media/1",
            vaultPath = "/vault/video_2026_01.mp4",
            mediaType = "VIDEO",
            fileExtension = "mp4",
            state = MediaState.DELETE,
            lastCopiedOffset = 100,
            createdTime = 1000,
            updatedTime = 1000,
            deletionScheduledAt = 2000
        )
        dao.insertRecord(record)

        val result = classUnderTest.recover("video_2026_01")
        assertTrue(result is MediaRecoveryManager.RecoveryResult.Recovered)

        val updatedRecord = dao.getRecord("video_2026_01")
        assertEquals(MediaState.COMPLETED, updatedRecord?.state)
        assertEquals(null, updatedRecord?.deletionScheduledAt)
    }

    @Test
    fun `recover rejected when file is not in DELETE state`() = runBlocking {
        val record = MediaRecord(
            fileId = "video_2026_01",
            originalUri = "content://media/1",
            vaultPath = "/vault/video_2026_01.mp4",
            mediaType = "VIDEO",
            fileExtension = "mp4",
            state = MediaState.COMPLETED,
            lastCopiedOffset = 100,
            createdTime = 1000,
            updatedTime = 1000,
            deletionScheduledAt = null
        )
        dao.insertRecord(record)

        val result = classUnderTest.recover("video_2026_01")
        assertTrue(result is MediaRecoveryManager.RecoveryResult.Rejected)
    }

    @Test
    fun `recoverBatch successful for valid files`() = runBlocking {
        dao.insertRecord(
            MediaRecord(
                fileId = "video_2026_01",
                originalUri = "",
                vaultPath = null,
                mediaType = "VIDEO",
                fileExtension = "mp4",
                state = MediaState.DELETE,
                lastCopiedOffset = 0,
                createdTime = 1000,
                updatedTime = 1000,
                deletionScheduledAt = 2000
            )
        )
        dao.insertRecord(
            MediaRecord(
                fileId = "video_2026_02",
                originalUri = "",
                vaultPath = null,
                mediaType = "VIDEO",
                fileExtension = "mp4",
                state = MediaState.DELETE,
                lastCopiedOffset = 0,
                createdTime = 1000,
                updatedTime = 1000,
                deletionScheduledAt = 2000
            )
        )

        val result = classUnderTest.recoverBatch(listOf("video_2026_01", "video_2026_02", "video_2026_03"))
        assertEquals(2, result.successfulIds.size)
        assertEquals(1, result.failedIds.size)
    }
}
