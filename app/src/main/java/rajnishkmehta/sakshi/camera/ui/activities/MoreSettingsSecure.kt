package rajnishkmehta.sakshi.camera.ui.activities

import android.os.Bundle
import rajnishkmehta.sakshi.camera.AutoFinishOnSleep

class MoreSettingsSecure : MoreSettings() {

    private val autoFinisher = AutoFinishOnSleep(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        autoFinisher.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        autoFinisher.stop()
    }
}
