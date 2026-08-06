package rajnishkmehta.sakshi.camera.vault

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    val hasLauncherActivity: Boolean
)
