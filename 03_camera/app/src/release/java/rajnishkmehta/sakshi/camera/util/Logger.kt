package rajnishkmehta.sakshi.camera.util

import android.content.Context

/**
 * A custom Logger implementation that allows logs to be saved to files in debug builds
 * and stripped completely in release builds to improve performance and security.
 */
object Logger {
    /**
     * Initializes the logger and prepares the log directory.
     *
     * @param context Application context used to retrieve the external files directory.
     */
    @JvmStatic fun init(context: Context) {
        // No-op in release
    }

    /**
     * Logs a debug message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     */
    @JvmStatic @JvmOverloads fun d(tag: String, msg: String, tr: Throwable? = null) {
        // No-op in release
    }

    /**
     * Logs an error message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     */
    @JvmStatic @JvmOverloads fun e(tag: String, msg: String, tr: Throwable? = null) {
        // No-op in release
    }

    /**
     * Logs a warning message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     */
    @JvmStatic @JvmOverloads fun w(tag: String, msg: String, tr: Throwable? = null) {
        // No-op in release
    }

    /**
     * Logs an info message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log.
     */
    @JvmStatic @JvmOverloads fun i(tag: String, msg: String, tr: Throwable? = null) {
        // No-op in release
    }
}
