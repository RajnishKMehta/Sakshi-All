/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.deletion

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import rajnishkmehta.sakshi.vault.db.MediaState
import java.io.File

class MediaFileCleanerTest {

    @TempDir
    lateinit var filesDir: File

    private val record get() = testRecord(state = MediaState.DELETE, deletionScheduledAt = 1L)
    private val mediaFile get() = File(filesDir, "media/photo/photo_00000001.jpg")
    private val thumbnail get() = File(filesDir, "media/photo/thumbnail/photo_00000001.webp")

    private fun create(file: File) {
        file.parentFile!!.mkdirs()
        file.writeText("data")
    }

    @Test
    fun `deletes media file and thumbnail`() {
        create(mediaFile)
        create(thumbnail)

        assertTrue(MediaFileCleaner(filesDir).deleteFiles(record))
        assertFalse(mediaFile.exists())
        assertFalse(thumbnail.exists())
    }

    @Test
    fun `already missing media and thumbnail count as deleted`() {
        assertTrue(MediaFileCleaner(filesDir).deleteFiles(record))
    }

    @Test
    fun `missing thumbnail does not block media deletion`() {
        create(mediaFile)
        assertTrue(MediaFileCleaner(filesDir).deleteFiles(record))
        assertFalse(mediaFile.exists())
    }

    @Test
    fun `reports failure when a target cannot be deleted`() {
        create(thumbnail)
        mediaFile.mkdirs()
        File(mediaFile, "child").writeText("x")

        assertFalse(MediaFileCleaner(filesDir).deleteFiles(record))
        assertFalse(thumbnail.exists())
    }

    @Test
    fun `never deletes files outside private media storage`() {
        val outside = File(filesDir.parentFile, "public_${System.nanoTime()}.jpg").apply { writeText("public copy") }
        try {
            val result = MediaFileCleaner(filesDir).deleteFiles(record.copy(vaultPath = outside.absolutePath))
            assertTrue(result)
            assertTrue(outside.exists())
        } finally {
            outside.delete()
        }
    }

    @Test
    fun `rejects invalid stored path components`() {
        val invalid = record.copy(fileId = "../../escape")
        assertFalse(MediaFileCleaner(filesDir).deleteFiles(invalid))
    }
}
