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

object Logger {
    private var logDir: File? = null

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
        val trace = tr?.let { "
" + Log.getStackTraceString(it) } ?: ""
        val logLine = "$timestamp $level/$tag: $msg$trace
"

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
            } catch (e: Exception) {
                Log.e("Logger", "Failed to write log", e)
            }
        }
    }


            specificFile?.let {
                FileWriter(File(logDir, it), true).use { writer -> writer.append(logLine) }
            }
        } catch (e: Exception) {
            Log.e("Logger", "Failed to write log", e)
        }
    }

    @JvmStatic @JvmOverloads fun d(tag: String, msg: String, tr: Throwable? = null) {
        Log.d(tag, msg)
        writeLog("D", tag, msg)
    }

    @JvmStatic @JvmOverloads fun e(tag: String, msg: String, tr: Throwable? = null) {
        if (tr != null) {
            Log.e(tag, msg, tr)
        } else {
            Log.e(tag, msg)
        }
        writeLog("E", tag, msg, tr)
    }

    @JvmStatic @JvmOverloads fun w(tag: String, msg: String, tr: Throwable? = null) {
        if (tr != null) {
            Log.w(tag, msg, tr)
        } else {
            Log.w(tag, msg)
        }
        writeLog("W", tag, msg, tr)
    }

    @JvmStatic @JvmOverloads fun i(tag: String, msg: String, tr: Throwable? = null) {
        Log.i(tag, msg)
        writeLog("I", tag, msg)
    }

    @JvmStatic fun getLogDir(): File? = logDir
}
