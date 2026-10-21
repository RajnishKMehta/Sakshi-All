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

    @Test
    public fun `deleteMedia single success calls service correctly`(): Unit = runBlocking {
        val mockConnection = mockk<VaultServiceConnection>(relaxed = true)
        val mockService = mockk<ISakshiVaultService>(relaxed = true)
        coEvery { mockConnection.getService() } returns SakshiResult.Success(mockService)

        val mockBundle = mockk<Bundle>(relaxed = true)
        every { mockBundle.getBoolean("success", false) } returns true

        every { mockService.deleteMedia(any()) } returns mockBundle

        val client = SakshiClientImpl(mockk<Context>(relaxed = true), SakshiClientConfig())

        // Inject mock connection using reflection
        val field = SakshiClientImpl::class.java.getDeclaredField("serviceConnection")
        field.isAccessible = true
        field.set(client, mockConnection)

        val result = client.deleteMedia("file_1")
        assertTrue(result is SakshiResult.Success)
    }

    @Test
    public fun `deleteMedia batch success calls service correctly`(): Unit = runBlocking {
        val mockConnection = mockk<VaultServiceConnection>(relaxed = true)
        val mockService = mockk<ISakshiVaultService>(relaxed = true)
        coEvery { mockConnection.getService() } returns SakshiResult.Success(mockService)

        val mockBundle = mockk<Bundle>(relaxed = true)
        every { mockBundle.getStringArrayList("successfulIds") } returns arrayListOf("file_1", "file_2")
        val mockFailedBundle = mockk<Bundle>(relaxed = true)
        every { mockBundle.getBundle("failedIds") } returns mockFailedBundle
        every { mockFailedBundle.keySet() } returns setOf("file_3")
        every { mockFailedBundle.getString("file_3") } returns "Not found"

        every { mockService.deleteMediaBatch(any()) } returns mockBundle

        val client = SakshiClientImpl(mockk<Context>(relaxed = true), SakshiClientConfig())

        // Inject mock connection using reflection
        val field = SakshiClientImpl::class.java.getDeclaredField("serviceConnection")
        field.isAccessible = true
        field.set(client, mockConnection)

        val result = client.deleteMedia(listOf("file_1", "file_2", "file_3"))
        assertTrue(result is SakshiResult.Success)
        val data = (result as SakshiResult.Success).data
        assertEquals(listOf("file_1", "file_2"), data.successfulIds)
        assertEquals(mapOf("file_3" to "Not found"), data.failedIds)
    }

    @Test
    public fun `deleteMedia batch empty list returns immediate success`(): Unit = runBlocking {
        val client = SakshiClientImpl(mockk<Context>(relaxed = true), SakshiClientConfig())
        val result = client.deleteMedia(emptyList())
        assertTrue(result is SakshiResult.Success)
        val data = (result as SakshiResult.Success).data
        assertTrue(data.successfulIds.isEmpty())
        assertTrue(data.failedIds.isEmpty())
    }

    @Test
    public fun `deleteMedia single failure returns error result`(): Unit = runBlocking {
        val mockConnection = mockk<VaultServiceConnection>(relaxed = true)
        val mockService = mockk<ISakshiVaultService>(relaxed = true)
        coEvery { mockConnection.getService() } returns SakshiResult.Success(mockService)

        val mockBundle = mockk<Bundle>(relaxed = true)
        every { mockBundle.getBoolean("success", false) } returns false
        every { mockBundle.getString("error") } returns "File not found"

        every { mockService.deleteMedia(any()) } returns mockBundle

        val client = SakshiClientImpl(mockk<Context>(relaxed = true), SakshiClientConfig())

        // Inject mock connection using reflection
        val field = SakshiClientImpl::class.java.getDeclaredField("serviceConnection")
        field.isAccessible = true
        field.set(client, mockConnection)

        val result = client.deleteMedia("file_missing")
        assertTrue(result is SakshiResult.Failure)
        val error = (result as SakshiResult.Failure).error
        assertTrue(error is SakshiError.IpcError)
        assertEquals("File not found", error.message)
    }

    @Test
    public fun `deleteMedia batch total failure returns error result`(): Unit = runBlocking {
        val mockConnection = mockk<VaultServiceConnection>(relaxed = true)
        val mockService = mockk<ISakshiVaultService>(relaxed = true)
        coEvery { mockConnection.getService() } returns SakshiResult.Success(mockService)

        val mockBundle = mockk<Bundle>(relaxed = true)
        every { mockBundle.getStringArrayList("successfulIds") } returns null
        every { mockBundle.getBundle("failedIds") } returns null
        every { mockBundle.containsKey("error") } returns true
        every { mockBundle.getString("error") } returns "Internal Server Error"

        every { mockService.deleteMediaBatch(any()) } returns mockBundle

        val client = SakshiClientImpl(mockk<Context>(relaxed = true), SakshiClientConfig())

        // Inject mock connection using reflection
        val field = SakshiClientImpl::class.java.getDeclaredField("serviceConnection")
        field.isAccessible = true
        field.set(client, mockConnection)

        val result = client.deleteMedia(listOf("file_1", "file_2"))
        assertTrue(result is SakshiResult.Failure)
        val error = (result as SakshiResult.Failure).error
        assertTrue(error is SakshiError.IpcError)
        assertEquals("Internal Server Error", error.message)
    }
}
