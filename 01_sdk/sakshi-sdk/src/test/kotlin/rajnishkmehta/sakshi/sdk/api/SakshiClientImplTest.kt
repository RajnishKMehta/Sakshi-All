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

public class SakshiClientImplTest {

    @Test
    public fun `getMedia calls service correctly`(): Unit = runBlocking {
        val mockConnection = mockk<VaultServiceConnection>(relaxed = true)
        val mockService = mockk<ISakshiVaultService>(relaxed = true)
        coEvery { mockConnection.getService() } returns SakshiResult.Success(mockService)
        every { mockService.getMedia(any(), any()) } returns "content://mock/media/{mediaType}/{fileId}"

        val client = SakshiClientImpl(mockk<Context>(relaxed = true), SakshiClientConfig())

        // Inject mock connection using reflection
        val field = SakshiClientImpl::class.java.getDeclaredField("serviceConnection")
        field.isAccessible = true
        field.set(client, mockConnection)

        val result = client.getMedia("VIDEO", "123")
        assertTrue(result is SakshiResult.Success)
        assertEquals("content://mock/media/{mediaType}/{fileId}", (result as SakshiResult.Success).data)
    }

    @Test
    public fun dummyTest(): Unit {
        assertTrue(true)
    }
}
