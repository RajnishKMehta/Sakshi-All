package rajnishkmehta.sakshi.sdk.api

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.every
import io.mockk.anyConstructed
import io.mockk.matchers.any
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultService
import rajnishkmehta.sakshi.sdk.internal.ipc.VaultServiceConnection
import rajnishkmehta.sakshi.sdk.internal.SakshiClientImpl
import android.content.Context

public class SakshiClientImplTest {
    @Test
    public fun `getMedia calls service correctly`(): Unit = runBlocking {
        mockkConstructor(VaultServiceConnection::class)
        val mockService = mockk<ISakshiVaultService>()
        coEvery { anyConstructed<VaultServiceConnection>().getService() } returns SakshiResult.Success(mockService)
        coEvery { mockService.getMedia(any<String>(), any<String>()) } returns "content://mock/media/{mediaType}/{fileId}"

        val client = SakshiClientImpl(mockk<Context>(relaxed = true), SakshiClientConfig())
        val result = client.getMedia("VIDEO", "123")
        assertTrue(result is SakshiResult.Success)
        assertEquals("content://mock/media/{mediaType}/{fileId}", (result as SakshiResult.Success).data)
    }

    @Test
    public fun dummyTest(): Unit {
        assertTrue(true)
    }
}
