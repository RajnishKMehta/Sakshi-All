package rajnishkmehta.sakshi.camera.logging

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Logger {
    private const val TAG = "AppLogger"
    private const val ALL_LOGS = "all_logs.txt"
    private const val ERROR_LOGS = "error_logs.txt"
    private const val WARNING_LOGS = "warning_logs.txt"
    private const val INFO_LOGS = "info_logs.txt"

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun getLogDir(): File? {
        val context = appContext ?: return null
        val dir = File(context.filesDir, "debug_logs")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun writeLog(fileName: String, message: String) {
        val dir = getLogDir() ?: return
        try {
            val file = File(dir, fileName)
            val writer = FileWriter(file, true)
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
            writer.append("[$timestamp] $message\n")
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write log to $fileName", e)
        }
    }

    private fun formatLog(tag: String, msg: String): String {
        return "[$tag] $msg"
    }

    fun i(tag: String, msg: String) {
        Log.i(tag, msg)
        val formattedMsg = formatLog(tag, msg)
        writeLog(INFO_LOGS, formattedMsg)
        writeLog(ALL_LOGS, "INFO: $formattedMsg")
    }

    fun e(tag: String, msg: String, throwable: Throwable? = null) {
        Log.e(tag, msg, throwable)
        val formattedMsg = formatLog(tag, msg + (throwable?.let { "\n${Log.getStackTraceString(it)}" } ?: ""))
        writeLog(ERROR_LOGS, formattedMsg)
        writeLog(ALL_LOGS, "ERROR: $formattedMsg")
    }

    fun w(tag: String, msg: String) {
        Log.w(tag, msg)
        val formattedMsg = formatLog(tag, msg)
        writeLog(WARNING_LOGS, formattedMsg)
        writeLog(ALL_LOGS, "WARN: $formattedMsg")
    }

    fun getLogFilesDir(context: Context): File {
        return File(context.filesDir, "debug_logs")
    }
}
