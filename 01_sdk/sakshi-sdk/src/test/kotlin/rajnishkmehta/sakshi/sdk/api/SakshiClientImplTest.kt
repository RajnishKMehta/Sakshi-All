/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.sdk.api

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.every
import kotlinx.coroutines.runBlocking
import rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultService
import rajnishkmehta.sakshi.sdk.internal.ipc.VaultServiceConnection
import rajnishkmehta.sakshi.sdk.internal.SakshiClientImpl
import android.content.Context
import android.os.Bundle
import rajnishkmehta.sakshi.sdk.api.models.MediaDetailsResponse

public class SakshiClientImplTest {

    @Test
    public fun `getMedia calls service correctly`(): Unit = runBlocking {
        val mockConnection = mockk<VaultServiceConnection>(relaxed = true)
        val mockService = mockk<ISakshiVaultService>(relaxed = true)
        coEvery { mockConnection.getService() } returns SakshiResult.Success(mockService)

        val mockBundle = mockk<Bundle>(relaxed = true)
        every { mockBundle.containsKey("contentUri") } returns true
        every { mockBundle.getString("contentUri") } returns "content://mock/media/video/123"
        every { mockBundle.getString("fileId") } returns "123"
        every { mockBundle.getString("mediaType") } returns "VIDEO"
        every { mockBundle.getString("fileExtension") } returns "mp4"
        every { mockBundle.getLong("createdTime", 0L) } returns 1600000000000L

        every { mockService.getMedia(any()) } returns mockBundle

        val client = SakshiClientImpl(mockk<Context>(relaxed = true), SakshiClientConfig())

        // Inject mock connection using reflection
        val field = SakshiClientImpl::class.java.getDeclaredField("serviceConnection")
        field.isAccessible = true
        field.set(client, mockConnection)

        val result = client.getMedia("123")
        assertTrue(result is SakshiResult.Success)

        val data = (result as SakshiResult.Success).data
        assertEquals("content://mock/media/video/123", data.contentUri)
        assertEquals("123", data.fileId)
        assertEquals("VIDEO", data.mediaType)
        assertEquals("mp4", data.fileExtension)
        assertEquals(1600000000000L, data.createdTime)
    }

    @Test
    public fun dummyTest(): Unit {
        assertTrue(true)
    }
}
