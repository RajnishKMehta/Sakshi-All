package rajnishkmehta.sakshi.vault.provider

import android.net.Uri
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.Robolectric
import android.content.pm.ProviderInfo
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import org.junit.runner.RunWith

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], manifest = Config.NONE)
class MediaProviderTest {

    private lateinit var provider: MediaProvider

    @BeforeEach
    fun setup() {
        provider = MediaProvider()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val info = ProviderInfo()
        info.authority = "${context.packageName}.mediaprovider"
        provider.attachInfo(context, info)
    }

    @Test
    fun `getType with invalid uri returns null`() {
        val uri = Uri.parse("content://invalid/uri")
        assertNull(provider.getType(uri))
    }
}
