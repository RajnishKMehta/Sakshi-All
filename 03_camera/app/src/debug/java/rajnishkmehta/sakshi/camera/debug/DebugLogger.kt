package rajnishkmehta.sakshi.camera.debug

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DebugLogger {
    private const val TAG = "SakshiCamera"
    private var logsDir: File? = null

    @JvmStatic
    fun init(context: Context) {
        logsDir = File(context.getExternalFilesDir(null), "debug_logs")
        if (logsDir?.exists() == false) {
            logsDir?.mkdirs()
        }
    }

    @JvmStatic
    fun d(tag: String = TAG, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.d(tag, message, throwable)
        } else {
            Log.d(tag, message)
        }
        writeToFile("info_logs.txt", "DEBUG", tag, message, throwable)
        writeToFile("all_logs.txt", "DEBUG", tag, message, throwable)
    }

    @JvmStatic
    fun i(tag: String = TAG, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.i(tag, message, throwable)
        } else {
            Log.i(tag, message)
        }
        writeToFile("info_logs.txt", "INFO", tag, message, throwable)
        writeToFile("all_logs.txt", "INFO", tag, message, throwable)
    }

    @JvmStatic
    fun w(tag: String = TAG, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.w(tag, message, throwable)
        } else {
            Log.w(tag, message)
        }
        writeToFile("warning_logs.txt", "WARN", tag, message, throwable)
        writeToFile("all_logs.txt", "WARN", tag, message, throwable)
    }

    @JvmStatic
    fun e(tag: String = TAG, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
        writeToFile("error_logs.txt", "ERROR", tag, message, throwable)
        writeToFile("all_logs.txt", "ERROR", tag, message, throwable)
    }

    @JvmStatic
    private fun writeToFile(fileName: String, level: String, tag: String, message: String, throwable: Throwable? = null) {
        if (logsDir == null) return
        try {
            val file = File(logsDir, fileName)
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            val logLine = "$timestamp [$level] $tag: $message\n"

            FileOutputStream(file, true).use {
                it.write(logLine.toByteArray())
                throwable?.let { t ->
                    it.write((Log.getStackTraceString(t) + "\n").toByteArray())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error writing to log file: $fileName", e)
        }
    }

    @JvmStatic
    fun getLogFiles(): List<File> {
        return logsDir?.listFiles()?.toList() ?: emptyList()
    }

    @JvmStatic
    fun clearLogs() {
        logsDir?.listFiles()?.forEach { it.delete() }
    }
}
