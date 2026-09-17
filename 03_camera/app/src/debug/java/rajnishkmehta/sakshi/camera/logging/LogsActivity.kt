package rajnishkmehta.sakshi.camera.logging

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import rajnishkmehta.sakshi.camera.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class LogsActivity : AppCompatActivity() {

    private var currentFileToExport: File? = null

    private val exportFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                currentFileToExport?.let { file ->
                    if (file.exists()) {
                        try {
                            contentResolver.openOutputStream(uri)?.use { outputStream ->
                                FileInputStream(file).use { inputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }
                            Toast.makeText(this, "Logs exported successfully", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this, "Failed to export logs: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this, "Log file is empty or does not exist", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        val logDir = Logger.getLogFilesDir(this)

        findViewById<Button>(R.id.btnExportAll).setOnClickListener {
            exportLogFile(File(logDir, "all_logs.txt"), "all_logs.txt")
        }

        findViewById<Button>(R.id.btnExportError).setOnClickListener {
            exportLogFile(File(logDir, "error_logs.txt"), "error_logs.txt")
        }

        findViewById<Button>(R.id.btnExportWarning).setOnClickListener {
            exportLogFile(File(logDir, "warning_logs.txt"), "warning_logs.txt")
        }

        findViewById<Button>(R.id.btnExportInfo).setOnClickListener {
            exportLogFile(File(logDir, "info_logs.txt"), "info_logs.txt")
        }

        findViewById<Button>(R.id.btnDeleteAll).setOnClickListener {
            var deletedCount = 0
            val files = listOf("all_logs.txt", "error_logs.txt", "warning_logs.txt", "info_logs.txt")
            for (fileName in files) {
                val file = File(logDir, fileName)
                if (file.exists() && file.delete()) {
                    deletedCount++
                }
            }
            Toast.makeText(this, "Deleted $deletedCount log file(s)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportLogFile(file: File, suggestedName: String) {
        if (!file.exists()) {
            Toast.makeText(this, "No logs available for this category.", Toast.LENGTH_SHORT).show()
            return
        }

        currentFileToExport = file
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, suggestedName)
        }
        exportFileLauncher.launch(intent)
    }
}
