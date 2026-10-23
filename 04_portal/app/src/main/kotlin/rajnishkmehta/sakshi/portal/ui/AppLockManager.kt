package rajnishkmehta.sakshi.portal.ui

import android.os.SystemClock
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rajnishkmehta.sakshi.portal.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import rajnishkmehta.sakshi.portal.debug.DebugLogger as Log

/**
 * Manages the app lock state based on lifecycle events and user settings.
 */
class AppLockManager(
    private val settingsRepository: SettingsRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : DefaultLifecycleObserver {

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private var backgroundTime: Long = 0L

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // When initialized, we might be starting fresh, so check if we should lock immediately
        coroutineScope.launch {
            val enabled = settingsRepository.appLockEnabledFlow.first()
            if (enabled) {
                _isLocked.value = true
                Log.i("AppLockManager", "App started, locking immediately.")
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        if (backgroundTime > 0) {
            val timeAway = SystemClock.elapsedRealtime() - backgroundTime

            coroutineScope.launch {
                val enabled = settingsRepository.appLockEnabledFlow.first()
                val timeout = settingsRepository.appLockTimeoutMsFlow.first()

                if (enabled && timeAway >= timeout) {
                    _isLocked.value = true
                    Log.i("AppLockManager", "App returned from background after \${timeAway}ms, locking.")
                }
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        backgroundTime = SystemClock.elapsedRealtime()
    }

    fun unlock() {
        _isLocked.value = false
        backgroundTime = 0L // Reset background time on unlock
        Log.i("AppLockManager", "App unlocked.")
    }

    fun lock() {
        coroutineScope.launch {
            val enabled = settingsRepository.appLockEnabledFlow.first()
            if (enabled) {
                _isLocked.value = true
                Log.i("AppLockManager", "App locked manually.")
            }
        }
    }
}
