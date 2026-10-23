/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import rajnishkmehta.sakshi.vault.db.MediaRecord
import rajnishkmehta.sakshi.vault.db.MediaState

class MediaListFilterTest {

    private fun record(fileId: String, state: String) = MediaRecord(
        fileId = fileId,
        originalUri = "content://camera/$fileId",
        vaultPath = null,
        mediaType = "PHOTO",
        fileExtension = "jpg",
        state = state,
        lastCopiedOffset = 0L,
        createdTime = 0L,
        updatedTime = 0L,
        deletionScheduledAt = if (state == MediaState.DELETE) 1L else null
    )

    @Test
    fun `only completed and failed records are listed`() {
        val records = listOf(
            MediaState.INITIALIZING, MediaState.SYNCING, MediaState.PAUSED, MediaState.COMPLETED,
            MediaState.STOPPED, MediaState.FAILED, MediaState.DELETE
        ).map { record(it.lowercase(), it) }

        assertEquals(listOf("completed", "failed"), filterListableRecords(records).map { it.fileId })
    }

    @Test
    fun `records scheduled for deletion are excluded`() {
        val records = listOf(record("kept_0001", MediaState.COMPLETED), record("gone_0001", MediaState.DELETE))
        assertEquals(listOf("kept_0001"), filterListableRecords(records).map { it.fileId })
    }

    @Test
    fun `formatMediaList uses correct timestamp selector for listMedia`() {
        val records = listOf(
            MediaRecord(
                fileId = "file1",
                originalUri = "",
                vaultPath = null,
                mediaType = "PHOTO",
                fileExtension = "jpg",
                state = MediaState.COMPLETED,
                lastCopiedOffset = 0,
                createdTime = 100,
                updatedTime = 150,
                deletionScheduledAt = 200
            )
        )
        val json = formatMediaList(records) { it.createdTime }
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"timestamp\":100"))
    }

    @Test
    fun `formatMediaList uses correct timestamp selector for listDeletedMedia`() {
        val records = listOf(
            MediaRecord(
                fileId = "file1",
                originalUri = "",
                vaultPath = null,
                mediaType = "PHOTO",
                fileExtension = "jpg",
                state = MediaState.DELETE,
                lastCopiedOffset = 0,
                createdTime = 100,
                updatedTime = 150,
                deletionScheduledAt = 200
            )
        )
        val json = formatMediaList(records) { it.deletionScheduledAt ?: it.updatedTime }
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"timestamp\":200"))
    }

}
