package rajnishkmehta.sakshi.camera

import android.app.Activity
import android.app.Application
import android.os.Bundle
import rajnishkmehta.sakshi.camera.ui.activities.MainActivity

/**
 * Helper class that monitors the application's activity lifecycle callbacks.
 * Specifically tracks the [MainActivity] instance to handle operations like
 * auto-sleep toggling when the activity resumes or pauses.
 *
 * @property callback A callback invoked with the [MainActivity] instance when it resumes,
 * or with null when it pauses.
 */
class ActivityLifeCycleHelper(
    private val callback: (activity: MainActivity?) -> Unit
) : Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        if (activity is MainActivity) {
            callback.invoke(activity)
        }
    }

    override fun onActivityPaused(activity: Activity) {
        if (activity is MainActivity) {
            callback.invoke(null)
        }
    }

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}
