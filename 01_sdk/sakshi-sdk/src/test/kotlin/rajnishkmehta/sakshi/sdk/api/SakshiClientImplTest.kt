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
    public fun dummyTest() {
        assertTrue(true)
    }
}
