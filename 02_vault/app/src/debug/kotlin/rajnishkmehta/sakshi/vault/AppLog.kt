package rajnishkmehta.sakshi.vault

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

object AppLog {
    private const val MAX_BUFFER_SIZE = 2000
    private val liveBuffer = ConcurrentLinkedQueue<String>()
    private val listeners = java.util.concurrent.CopyOnWriteArrayList<(String) -> Unit>()

    @Volatile
    private var currentFileWriter: PrintWriter? = null
    @Volatile
    private var currentFile: File? = null

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val fileTimeFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    fun getCollectedLogsDir(context: Context): File {
        val dir = File(context.filesDir, "collected_logs")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun addListener(listener: (String) -> Unit) {
        listeners.add(listener)
        // Send existing buffer
        for (log in liveBuffer) {
            listener(log)
        }
    }

    fun removeListener(listener: (String) -> Unit) {
        listeners.remove(listener)
    }

    fun startCollecting(context: Context): String {
        synchronized(this) {
            stopCollecting()
            val dir = getCollectedLogsDir(context)
            val fileName = "log_${fileTimeFormat.format(Date())}.txt"
            val file = File(dir, fileName)
            val writer = PrintWriter(FileWriter(file, true))
            currentFileWriter = writer
            currentFile = file
            d("AppLog", "Started collecting logs to: ${file.name}")
            return file.name
        }
    }

    fun stopCollecting() {
        synchronized(this) {
            currentFileWriter?.let {
                d("AppLog", "Stopped collecting logs.")
                it.flush()
                it.close()
            }
            currentFileWriter = null
            currentFile = null
        }
    }

    fun isCollecting(): Boolean {
        return currentFileWriter != null
    }

    fun getCurrentCollectionFileName(): String? {
        return currentFile?.name
    }

    fun getLiveLogs(): List<String> {
        return liveBuffer.toList()
    }

    fun clearLiveLogs() {
        liveBuffer.clear()
    }

    fun d(tag: String, msg: String) {
        Log.d(tag, msg)
        logLine("D", tag, msg)
    }

    fun e(tag: String, msg: String, tr: Throwable? = null) {
        Log.e(tag, msg, tr)
        val fullMsg = if (tr != null) "$msg\n${Log.getStackTraceString(tr)}" else msg
        logLine("E", tag, fullMsg)
    }

    private fun logLine(level: String, tag: String, msg: String) {
        val timeStamp = timeFormat.format(Date())
        val formattedLine = "$timeStamp [$level/$tag]: $msg"

        liveBuffer.add(formattedLine)
        while (liveBuffer.size > MAX_BUFFER_SIZE) {
            liveBuffer.poll()
        }

        for (listener in listeners) {
            try {
                listener(formattedLine)
            } catch (ignored: Exception) {}
        }

        synchronized(this) {
            currentFileWriter?.let { writer ->
                try {
                    writer.println(formattedLine)
                    writer.flush()
                } catch (ignored: Exception) {}
            }
        }
    }
}
