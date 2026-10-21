package rajnishkmehta.sakshi.camera

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import rajnishkmehta.sakshi.camera.CamConfig.SettingValues.Default.FORMAT_FMP4
import rajnishkmehta.sakshi.camera.CamConfig.SettingValues.Default.FORMAT_MPEG4
import rajnishkmehta.sakshi.camera.ui.Mp4WarningDialog
import rajnishkmehta.sakshi.camera.ui.OptionsSelectionDialog
import rajnishkmehta.sakshi.camera.ui.activities.MainActivity
import rajnishkmehta.sakshi.camera.ui.activities.MoreSettings
import rajnishkmehta.sakshi.camera.vault.VideoVaultTransferMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VideoFormatSelectionTest {

    @get:Rule
    val grantPermissions: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA,
    )

    /** More Settings is started from a resumed camera, which a dozing device cannot provide. */
    @get:Rule
    val screenAwake = ScreenAwakeRule()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    /** fMP4 keeps incremental sync; only normal MP4 falls back to a single copy after recording. */
    @Test
    fun transferMode_followsVideoFormat() {
        assertEquals(VideoVaultTransferMode.INCREMENTAL_SYNC, VideoVaultTransferMode.forVideoFormat(FORMAT_FMP4))
        assertEquals(VideoVaultTransferMode.COPY_AFTER_RECORDING, VideoVaultTransferMode.forVideoFormat(FORMAT_MPEG4))
    }

    @Test
    fun choosingMp4_showsWarningWithConfirmLeftOfCancel_andCancelKeepsFmp4() {
        withMoreSettings(FORMAT_FMP4) { camConfig, settings ->
            val dialog = chooseMp4(settings)
            assertNotNull("The MP4 warning was not shown", dialog)
            assertEquals(FORMAT_FMP4, camConfig.videoFormat)

            instrumentation.runOnMainSync {
                val confirm = dialog!!.requireDialog().findViewById<View>(R.id.dialog_button_left)
                val cancel = dialog.requireDialog().findViewById<View>(R.id.dialog_button_right)
                assertTrue("Confirm must be left of Cancel", screenX(confirm) < screenX(cancel))
                cancel.performClick()
            }
            instrumentation.waitForIdleSync()

            assertEquals(FORMAT_FMP4, camConfig.videoFormat)
        }
    }

    @Test
    fun choosingMp4_andConfirming_selectsMp4() {
        withMoreSettings(FORMAT_FMP4) { camConfig, settings ->
            val dialog = chooseMp4(settings)
            assertNotNull("The MP4 warning was not shown", dialog)

            instrumentation.runOnMainSync {
                dialog!!.requireDialog().findViewById<View>(R.id.dialog_button_left).performClick()
            }
            instrumentation.waitForIdleSync()

            assertEquals(FORMAT_MPEG4, camConfig.videoFormat)
        }
    }

    @Test
    fun choosingFmp4_doesNotShowWarning() {
        withMoreSettings(FORMAT_MPEG4) { camConfig, settings ->
            instrumentation.runOnMainSync {
                settings.supportFragmentManager.setFragmentResult(
                    VIDEO_FORMAT_REQUEST_KEY,
                    selectionResult(0),
                )
            }
            instrumentation.waitForIdleSync()

            assertNull(settings.supportFragmentManager.findFragmentByTag(Mp4WarningDialog.TAG))
            assertEquals(FORMAT_FMP4, camConfig.videoFormat)
        }
    }

    private fun selectionResult(index: Int) =
        Bundle().apply { putInt(OptionsSelectionDialog.RESULT_SELECTED_INDEX, index) }

    private fun chooseMp4(settings: MoreSettings): DialogFragment? {
        instrumentation.runOnMainSync {
            settings.supportFragmentManager.setFragmentResult(
                VIDEO_FORMAT_REQUEST_KEY,
                selectionResult(1),
            )
            settings.supportFragmentManager.executePendingTransactions()
        }
        instrumentation.waitForIdleSync()
        return settings.supportFragmentManager.findFragmentByTag(Mp4WarningDialog.TAG) as DialogFragment?
    }

    private fun screenX(view: View): Int {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return location[0]
    }

    private fun withMoreSettings(initialFormat: Int, block: (CamConfig, MoreSettings) -> Unit) {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitUntil(scenario, "camera is bound") { it.camConfig.camera != null }

            lateinit var camConfig: CamConfig
            scenario.onActivity { camConfig = it.camConfig }
            val stored = camConfig.videoFormat
            camConfig.videoFormat = initialFormat

            val monitor = instrumentation.addMonitor(MoreSettings::class.java.name, null, false)
            scenario.onActivity { MoreSettings.start(it) }
            val settings = instrumentation.waitForMonitorWithTimeout(monitor, 10_000) as MoreSettings?
            assertNotNull("More Settings did not open", settings)

            try {
                block(camConfig, settings!!)
            } finally {
                settings?.finish()
                camConfig.videoFormat = stored
            }
        }
    }

    private companion object {
        const val VIDEO_FORMAT_REQUEST_KEY = "video_format_selection"
    }
}
