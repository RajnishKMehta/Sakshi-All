package rajnishkmehta.sakshi.sdk.api

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultService
import rajnishkmehta.sakshi.sdk.internal.ipc.VaultServiceConnection
import rajnishkmehta.sakshi.sdk.internal.SakshiClientImpl

public class SakshiClientImplTest {
    @Test
    public fun `getMedia calls service correctly`() = runBlocking {
        val mockConnection = mockk<VaultServiceConnection>()
        val mockService = mockk<ISakshiVaultService>()
        coEvery { mockConnection.getService() } returns Result.success(mockService)
        coEvery { mockService.getMedia(any(), any()) } returns "content://mock/media/{mediaType}/{fileId}"

        val client = SakshiClientImpl(mockk(relaxed = true), SakshiClientConfig(), mockConnection)
        val result = client.getMedia("VIDEO", "123")
        assertTrue(result is SakshiResult.Success)
        kotlin.test.assertEquals("content://mock/media/{mediaType}/{fileId}", (result as SakshiResult.Success).data)
    }

    @Test
    public fun dummyTest() {
        assertTrue(true)
    }
}
