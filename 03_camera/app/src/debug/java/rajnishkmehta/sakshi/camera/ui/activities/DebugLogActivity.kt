package rajnishkmehta.sakshi.camera.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import rajnishkmehta.sakshi.camera.R
import rajnishkmehta.sakshi.camera.util.Logger
import java.io.File

class DebugLogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val btnAll = Button(this).apply { text = "Export All Logs"; setOnClickListener { exportLog("all_logs.txt") } }
        val btnError = Button(this).apply { text = "Export Error Logs"; setOnClickListener { exportLog("error_logs.txt") } }
        val btnWarning = Button(this).apply { text = "Export Warning Logs"; setOnClickListener { exportLog("warning_logs.txt") } }
        val btnInfo = Button(this).apply { text = "Export Info Logs"; setOnClickListener { exportLog("info_logs.txt") } }
        val btnDelete = Button(this).apply { text = "Delete All Logs"; setOnClickListener { deleteLogs() } }

        layout.addView(btnAll)
        layout.addView(btnError)
        layout.addView(btnWarning)
        layout.addView(btnInfo)
        layout.addView(btnDelete)

        setContentView(layout)
    }

    private fun exportLog(filename: String) {
        val logDir = Logger.getLogDir() ?: return
        val file = File(logDir, filename)
        if (!file.exists()) {
            Toast.makeText(this, "Log file not found", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Export Log"))
        } catch (e: Exception) {
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteLogs() {
        val logDir = Logger.getLogDir() ?: return
        logDir.listFiles()?.forEach { it.delete() }
        Toast.makeText(this, "Logs deleted", Toast.LENGTH_SHORT).show()
    }
}
