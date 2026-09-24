package rajnishkmehta.sakshi.vault.provider

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class MediaProviderTest {

    @Test
    fun `provider can be instantiated`() {
        val provider = MediaProvider()
        assertNotNull(provider)
    }
}
