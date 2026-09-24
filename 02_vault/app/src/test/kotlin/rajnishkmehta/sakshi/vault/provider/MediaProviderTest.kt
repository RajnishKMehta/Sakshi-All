package rajnishkmehta.sakshi.vault.provider

import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MediaProviderTest {

    private lateinit var provider: MediaProvider

    @BeforeEach
    fun setup() {
        provider = MediaProvider()
        mockkStatic(Uri::class)
    }

    @AfterEach
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `provider throws SecurityException on insert`() {
        val mockUri = mockk<Uri>()
        assertThrows(SecurityException::class.java) {
            provider.insert(mockUri, null)
        }
    }

    @Test
    fun `provider throws SecurityException on delete`() {
        val mockUri = mockk<Uri>()
        assertThrows(SecurityException::class.java) {
            provider.delete(mockUri, null, null)
        }
    }

    @Test
    fun `provider throws SecurityException on update`() {
        val mockUri = mockk<Uri>()
        assertThrows(SecurityException::class.java) {
            provider.update(mockUri, null, null, null)
        }
    }

    @Test
    fun `provider throws SecurityException on write mode openFile`() {
        val mockUri = mockk<Uri>()
        assertThrows(SecurityException::class.java) {
            provider.openFile(mockUri, "w")
        }
    }
}
