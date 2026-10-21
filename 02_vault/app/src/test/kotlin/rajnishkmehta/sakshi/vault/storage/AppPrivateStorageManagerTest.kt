/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.storage

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.File

class AppPrivateStorageManagerTest {

    @TempDir
    lateinit var filesDir: File

    private lateinit var storageManager: AppPrivateStorageManager

    @BeforeEach
    fun setup() {
        val context = mockk<Context>()
        every { context.filesDir } returns filesDir
        storageManager = AppPrivateStorageManager(context)
    }

    private fun destination(): File =
        File(storageManager.getDestinationUri(FILE_ID, MEDIA_TYPE, EXTENSION))

    private fun append(source: ByteArray, offset: Long): Long =
        storageManager.appendMediaBytes(FILE_ID, ByteArrayInputStream(source), offset, MEDIA_TYPE, EXTENSION)

    @Test
    fun `first pass copies the whole source`() {
        val source = "HEADER-DATA".toByteArray()

        val copied = append(source, 0)

        assertEquals(source.size.toLong(), copied)
        assertArrayEquals(source, destination().readBytes())
    }

    @Test
    fun `later pass appends only new bytes and does not rewrite the beginning`() {
        destination().writeBytes("OLDHEAD-".toByteArray())
        val source = "NEWHEAD-TAIL".toByteArray()

        val copied = append(source, 8)

        assertEquals(4L, copied)
        assertArrayEquals("OLDHEAD-TAIL".toByteArray(), destination().readBytes())
    }

    @Test
    fun `destination longer than offset is truncated before appending`() {
        destination().writeBytes("ABCDpartial".toByteArray())
        val source = "ABCDEFGH".toByteArray()

        val copied = append(source, 4)

        assertEquals(4L, copied)
        assertArrayEquals("ABCDEFGH".toByteArray(), destination().readBytes())
    }

    @Test
    fun `pass with no new bytes copies nothing`() {
        destination().writeBytes("ABCD".toByteArray())

        val copied = append("ABCD".toByteArray(), 4)

        assertEquals(0L, copied)
        assertArrayEquals("ABCD".toByteArray(), destination().readBytes())
    }

    private companion object {
        const val FILE_ID = "vid_test1234"
        const val MEDIA_TYPE = "VIDEO"
        const val EXTENSION = "mp4"
    }
}
