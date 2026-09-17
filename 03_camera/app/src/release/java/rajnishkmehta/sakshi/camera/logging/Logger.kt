package rajnishkmehta.sakshi.camera.logging

import android.content.Context
import java.io.File

object Logger {
    fun init(context: Context) {}

    fun i(tag: String, msg: String) {}

    fun e(tag: String, msg: String, throwable: Throwable? = null) {}

    fun w(tag: String, msg: String) {}

    fun getLogFilesDir(context: Context): File {
        return File(context.filesDir, "debug_logs")
    }
}
