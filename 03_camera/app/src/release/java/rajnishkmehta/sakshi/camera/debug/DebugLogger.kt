package rajnishkmehta.sakshi.camera.debug

import android.content.Context
import android.util.Log

object DebugLogger {
    @JvmStatic
    fun init(context: Context) {
        // No-op in release
    }

    @JvmStatic
    fun d(tag: String = "SakshiCamera", message: String, throwable: Throwable? = null) {
        // No-op in release
    }

    @JvmStatic
    fun i(tag: String = "SakshiCamera", message: String, throwable: Throwable? = null) {
        // No-op in release
    }

    @JvmStatic
    fun w(tag: String = "SakshiCamera", message: String, throwable: Throwable? = null) {
        // No-op in release
    }

    @JvmStatic
    fun e(tag: String = "SakshiCamera", message: String, throwable: Throwable? = null) {
        // No-op in release
    }
}
