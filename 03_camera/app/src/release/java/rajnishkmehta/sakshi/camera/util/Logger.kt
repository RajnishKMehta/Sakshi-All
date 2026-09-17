package rajnishkmehta.sakshi.camera.util

import android.content.Context

object Logger {
    @JvmStatic fun init(context: Context) {
        // No-op in release
    }

    @JvmStatic @JvmOverloads fun d(tag: String, msg: String, tr: Throwable? = null) {
        // No-op in release
    }

    @JvmStatic @JvmOverloads fun e(tag: String, msg: String, tr: Throwable? = null) {
        // No-op in release
    }

    @JvmStatic @JvmOverloads fun w(tag: String, msg: String, tr: Throwable? = null) {
        // No-op in release
    }

    @JvmStatic @JvmOverloads fun i(tag: String, msg: String, tr: Throwable? = null) {
        // No-op in release
    }
}
