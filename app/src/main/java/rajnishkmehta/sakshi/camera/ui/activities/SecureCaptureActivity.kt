package rajnishkmehta.sakshi.camera.ui.activities

import android.content.SharedPreferences
import rajnishkmehta.sakshi.camera.util.EphemeralSharedPrefsNamespace
import rajnishkmehta.sakshi.camera.util.getPrefs

class SecureCaptureActivity : CaptureActivity(), SecureActivity {
    val ephemeralPrefsNamespace = EphemeralSharedPrefsNamespace()

    override fun getSharedPreferences(name: String, mode: Int): SharedPreferences {
        return ephemeralPrefsNamespace.getPrefs(this, name, mode, cloneOriginal = true)
    }
}
