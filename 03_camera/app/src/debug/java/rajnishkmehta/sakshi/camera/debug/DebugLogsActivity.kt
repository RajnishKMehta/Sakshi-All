package rajnishkmehta.sakshi.camera.debug

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import rajnishkmehta.sakshi.camera.R
import java.io.File

class DebugLogsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val title = android.widget.TextView(this).apply {
            text = "Debug Logs"
            textSize = 24f
            setPadding(0, 0, 0, 32)
        }
        layout.addView(title)

        val allLogsBtn = createExportButton("Export All Logs", "all_logs.txt")
        layout.addView(allLogsBtn)

        val errorLogsBtn = createExportButton("Export Error Logs", "error_logs.txt")
        layout.addView(errorLogsBtn)

        val warningLogsBtn = createExportButton("Export Warning Logs", "warning_logs.txt")
        layout.addView(warningLogsBtn)

        val infoLogsBtn = createExportButton("Export Info Logs", "info_logs.txt")
        layout.addView(infoLogsBtn)

        val clearBtn = Button(this).apply {
            text = "Clear All Logs"
            setOnClickListener {
                DebugLogger.clearLogs()
                Toast.makeText(this@DebugLogsActivity, "Logs cleared", Toast.LENGTH_SHORT).show()
            }
        }
        layout.addView(clearBtn)

        setContentView(layout)
    }

    private fun createExportButton(label: String, fileName: String): Button {
        return Button(this).apply {
            text = label
            setOnClickListener {
                exportLogFile(fileName)
            }
        }
    }

    private fun exportLogFile(fileName: String) {
        val files = DebugLogger.getLogFiles()
        val fileToExport = files.find { it.name == fileName }

        if (fileToExport != null && fileToExport.exists() && fileToExport.length() > 0) {
            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.debug.provider",
                fileToExport
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, "Share Log File"))
        } else {
            Toast.makeText(this, "Log file is empty or does not exist", Toast.LENGTH_SHORT).show()
        }
    }
}
