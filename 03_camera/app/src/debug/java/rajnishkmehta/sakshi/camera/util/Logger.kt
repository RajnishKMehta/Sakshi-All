package rajnishkmehta.sakshi.camera.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.Executors
import java.util.Locale

/**
 * A custom Logger implementation that allows logs to be saved to files in debug builds
 * and stripped completely in release builds to improve performance and security.
 */
object Logger {
    private var logDir: File? = null

    /**
     * Initializes the logger and prepares the log directory.
     *
     * @param context Application context used to retrieve the external files directory.
     */
    @JvmStatic fun init(context: Context) {
        logDir = File(context.getExternalFilesDir(null), "logs")
        if (logDir?.exists() == false) {
            logDir?.mkdirs()
        }
    }


    private val executor = Executors.newSingleThreadExecutor()


    private fun writeLog(level: String, tag: String, msg: String, tr: Throwable? = null) {
        if (logDir == null) return

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
        val trace = tr?.let { "\n" + Log.getStackTraceString(it) } ?: ""
        val logLine = "$timestamp $level/$tag: $msg$trace\n"


        executor.execute {
            try {
                // Write to all logs
                FileWriter(File(logDir, "all_logs.txt"), true).use { it.append(logLine) }

                // Write to specific level log
                val specificFile = when(level) {
                    "E" -> "error_logs.txt"
                    "W" -> "warning_logs.txt"
                    "I" -> "info_logs.txt"
                    else -> null
                }

                specificFile?.let {
                    FileWriter(File(logDir, it), true).use { writer -> writer.append(logLine) }
                }

                specificFile?.let {
                    FileWriter(File(logDir, it), true).use { writer -> writer.append(logLine) }
                }


            specificFile?.let {
                FileWriter(File(logDir, it), true).use { writer -> writer.append(logLine) }
            }
        } catch (e: Exception) {
                Log.e("Logger", "Failed to write log", e)
            }
        }
    }

    /**
     * Logs a debug message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     */
    @JvmStatic @JvmOverloads fun d(tag: String, msg: String, tr: Throwable? = null) {
        Log.d(tag, msg)
        writeLog("D", tag, msg)
    }

    /**
     * Logs an error message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     */
    @JvmStatic @JvmOverloads fun e(tag: String, msg: String, tr: Throwable? = null) {
        if (tr != null) {
            Log.e(tag, msg, tr)
        } else {
            Log.e(tag, msg)
        }
        writeLog("E", tag, msg, tr)
    }

    /**
     * Logs a warning message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     */
    @JvmStatic @JvmOverloads fun w(tag: String, msg: String, tr: Throwable? = null) {
        if (tr != null) {
            Log.w(tag, msg, tr)
        } else {
            Log.w(tag, msg)
        }
        writeLog("W", tag, msg, tr)
    }

    /**
     * Logs an info message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     */
    @JvmStatic @JvmOverloads fun i(tag: String, msg: String, tr: Throwable? = null) {
        Log.i(tag, msg)
        writeLog("I", tag, msg)
    }

    @JvmStatic fun getLogDir(): File? = logDir
}
