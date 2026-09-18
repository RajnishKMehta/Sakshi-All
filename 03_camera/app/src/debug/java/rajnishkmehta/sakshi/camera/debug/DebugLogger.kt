package rajnishkmehta.sakshi.camera.debug

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A specialized logger used exclusively in debug builds for the Sakshi Camera application.
 *
 * This object manages the categorization and persistent storage of logs to the internal storage.
 * It writes all logs to an `all_logs.txt` file and categorizes them into `info_logs.txt`,
 * `warning_logs.txt`, and `error_logs.txt` based on the log level.
 */
object DebugLogger {
    private const val TAG = "SakshiCamera"
    private var logsDir: File? = null

    /**
     * Initializes the debug logger and creates the destination directory for log files if it does not exist.
     *
     * @param context The application context used to resolve the external files directory.
     */
    @JvmStatic
    fun init(context: Context) {
        logsDir = File(context.getExternalFilesDir(null), "debug_logs")
        if (logsDir?.exists() == false) {
            logsDir?.mkdirs()
        }
    }

    /**
     * Logs a debug message and optionally a throwable. The log is written to the system logcat
     * and persisted to the debug log files.
     *
     * @param tag Used to identify the source of a log message.
     * @param message The message you would like logged.
     * @param throwable An exception to log.
     */
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

    /**
     * Logs an info message and optionally a throwable. The log is written to the system logcat
     * and persisted to the info and all log files.
     *
     * @param tag Used to identify the source of a log message.
     * @param message The message you would like logged.
     * @param throwable An exception to log.
     */
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

    /**
     * Logs a warning message and optionally a throwable. The log is written to the system logcat
     * and persisted to the warning and all log files.
     *
     * @param tag Used to identify the source of a log message.
     * @param message The message you would like logged.
     * @param throwable An exception to log.
     */
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

    /**
     * Logs an error message and optionally a throwable. The log is written to the system logcat
     * and persisted to the error and all log files.
     *
     * @param tag Used to identify the source of a log message.
     * @param message The message you would like logged.
     * @param throwable An exception to log.
     */
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

    /**
     * Internal helper function to append a log line to a specific file.
     *
     * @param fileName The name of the file to write to.
     * @param level The log level (e.g., DEBUG, INFO, WARN, ERROR).
     * @param tag The log tag.
     * @param message The log message.
     * @param throwable An optional exception.
     */
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

    /**
     * Retrieves the list of generated log files from the debug directory.
     *
     * @return A list of [File] objects containing the logs. Returns an empty list if the directory does not exist or is empty.
     */
    @JvmStatic
    fun getLogFiles(): List<File> {
        return logsDir?.listFiles()?.toList() ?: emptyList()
    }

    /**
     * Deletes all currently generated log files from the debug directory.
     */
    @JvmStatic
    fun clearLogs() {
        logsDir?.listFiles()?.forEach { it.delete() }
    }
}
