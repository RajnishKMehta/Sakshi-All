package rajnishkmehta.sakshi.vault.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaRecordTest {

    @Test
    fun `vaultAddedTimestamp is initialized to current time`() {
        val beforeCreation = System.currentTimeMillis()
        val record = MediaRecord(
            fileId = "test_file_id",
            originalUri = "content://test/uri",
            vaultPath = "/vault/path",
            mediaType = "PHOTO",
            fileExtension = "jpg",
            completionState = "COMPLETED",
            lastCopiedOffset = 100L,
            createdTime = beforeCreation,
            updatedTime = beforeCreation
        )
        val afterCreation = System.currentTimeMillis()

        assertTrue(
            "vaultAddedTimestamp should be >= beforeCreation",
            record.vaultAddedTimestamp >= beforeCreation
        )
        assertTrue(
            "vaultAddedTimestamp should be <= afterCreation",
            record.vaultAddedTimestamp <= afterCreation
        )
    }

    @Test
    fun `vaultAddedTimestamp is preserved on copy`() {
        val originalRecord = MediaRecord(
            fileId = "test_file_id",
            originalUri = "content://test/uri",
            vaultPath = "/vault/path",
            mediaType = "PHOTO",
            fileExtension = "jpg",
            completionState = "INITIALIZING",
            lastCopiedOffset = 0L,
            createdTime = 1000L,
            updatedTime = 1000L
        )

        // Simulate some delay to ensure currentTimeMillis would have advanced
        Thread.sleep(10)

        val updatedRecord = originalRecord.copy(
            completionState = "COMPLETED",
            updatedTime = System.currentTimeMillis()
        )

        assertEquals(
            "vaultAddedTimestamp should be preserved on copy",
            originalRecord.vaultAddedTimestamp,
            updatedRecord.vaultAddedTimestamp
        )
    }
}
