package rajnishkmehta.sakshi.vault.debug

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import rajnishkmehta.sakshi.vault.AppLog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DebugLogActivity : Activity() {

    private lateinit var rootLayout: LinearLayout
    private lateinit var titleTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var startStopBtn: Button
    private lateinit var viewLogsBtn: Button
    private lateinit var clearBtn: Button

    private lateinit var liveLogsScroll: ScrollView
    private lateinit var liveLogsTextView: TextView

    private lateinit var fileListScroll: ScrollView
    private lateinit var fileListContainer: LinearLayout

    private var isViewingFiles = false

    private val logListener: (String) -> Unit = { line ->
        runOnUiThread {
            if (!isViewingFiles) {
                liveLogsTextView.append(line + "\n")
                if (liveLogsTextView.length() > 50000) {
                    val currentText = liveLogsTextView.text.toString()
                    liveLogsTextView.text = currentText.substring(currentText.length - 20000)
                }
                liveLogsScroll.post {
                    liveLogsScroll.fullScroll(View.FOCUS_DOWN)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bgColor = getThemeColor(android.R.attr.windowBackground, Color.parseColor("#121212"))
        val textColorPrimary = getThemeColor(android.R.attr.textColorPrimary, Color.WHITE)
        val textColorSecondary = getThemeColor(android.R.attr.textColorSecondary, Color.GRAY)
        val accentColor = getThemeColor(android.R.attr.colorAccent, Color.parseColor("#3F51B5"))

        window.decorView.setBackgroundColor(bgColor)

        rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 32
            }
        }

        titleTextView = TextView(this).apply {
            text = "Sakshi Vault Debug Console"
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
            setTextColor(textColorPrimary)
        }
        headerLayout.addView(titleTextView)

        statusTextView = TextView(this).apply {
            text = "Status: Idle"
            textSize = 14f
            setTextColor(textColorSecondary)
            setPadding(0, 8, 0, 0)
        }
        headerLayout.addView(statusTextView)
        rootLayout.addView(headerLayout)

        val controlsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 32
            }
        }

        val btnParams = LinearLayout.LayoutParams(0, dpToPx(44f), 1f).apply {
            rightMargin = 12
        }

        startStopBtn = Button(this).apply {
            text = "Start Collecting"
            layoutParams = btnParams
            setAllCaps(false)
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setTextColor(if (isColorDark(accentColor)) Color.WHITE else Color.BLACK)
            background = createRoundedDrawable(accentColor, dpToPx(8f).toFloat())
            setOnClickListener { toggleCollection() }
        }
        controlsLayout.addView(startStopBtn)

        viewLogsBtn = Button(this).apply {
            text = "View Logs File"
            layoutParams = btnParams
            setAllCaps(false)
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setTextColor(textColorPrimary)
            background = createRoundedDrawable(if (isColorDark(bgColor)) Color.parseColor("#2D2D2D") else Color.parseColor("#E0E0E0"), dpToPx(8f).toFloat())
            setOnClickListener { toggleViewMode() }
        }
        controlsLayout.addView(viewLogsBtn)

        clearBtn = Button(this).apply {
            text = "Clear"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dpToPx(44f)
            )
            setPadding(dpToPx(16f), 0, dpToPx(16f), 0)
            setAllCaps(false)
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setTextColor(textColorPrimary)
            background = createRoundedDrawable(if (isColorDark(bgColor)) Color.parseColor("#2D2D2D") else Color.parseColor("#E0E0E0"), dpToPx(8f).toFloat())
            setOnClickListener { clearScreen() }
        }
        controlsLayout.addView(clearBtn)

        rootLayout.addView(controlsLayout)

        liveLogsScroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            background = createRoundedDrawable(if (isColorDark(bgColor)) Color.parseColor("#1A1A1A") else Color.parseColor("#F5F5F5"), dpToPx(8f).toFloat())
            setPadding(24, 24, 24, 24)
        }

        liveLogsTextView = TextView(this).apply {
            textSize = 12f
            setTypeface(Typeface.MONOSPACE)
            setTextColor(textColorPrimary)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }
        liveLogsScroll.addView(liveLogsTextView)
        rootLayout.addView(liveLogsScroll)

        fileListScroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            visibility = View.GONE
        }

        fileListContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }
        fileListScroll.addView(fileListContainer)
        rootLayout.addView(fileListScroll)

        setContentView(rootLayout)

        updateStatus()

        AppLog.addListener(logListener)
    }

    override fun onDestroy() {
        AppLog.removeListener(logListener)
        super.onDestroy()
    }

    private fun getThemeColor(attr: Int, defaultColor: Int): Int {
        try {
            val typedValue = TypedValue()
            val a = theme.obtainStyledAttributes(typedValue.data, intArrayOf(attr))
            val color = a.getColor(0, defaultColor)
            a.recycle()
            return color
        } catch (e: Exception) {
            return defaultColor
        }
    }

    private fun isColorDark(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()
    }

    private fun createRoundedDrawable(color: Int, radius: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(color)
        }
    }

    private fun updateStatus() {
        val accentColor = getThemeColor(android.R.attr.colorAccent, Color.parseColor("#3F51B5"))
        if (AppLog.isCollecting()) {
            statusTextView.text = "Status: Collecting logs to ${AppLog.getCurrentCollectionFileName()}"
            statusTextView.setTextColor(Color.parseColor("#4CAF50"))
            startStopBtn.text = "Stop Collecting"
            startStopBtn.setTextColor(Color.WHITE)
            startStopBtn.background = createRoundedDrawable(Color.parseColor("#E53935"), dpToPx(8f).toFloat())
        } else {
            statusTextView.text = "Status: Idle"
            statusTextView.setTextColor(getThemeColor(android.R.attr.textColorSecondary, Color.GRAY))
            startStopBtn.text = "Start Collecting"
            startStopBtn.setTextColor(if (isColorDark(accentColor)) Color.WHITE else Color.BLACK)
            startStopBtn.background = createRoundedDrawable(accentColor, dpToPx(8f).toFloat())
        }
    }

    private fun toggleCollection() {
        if (AppLog.isCollecting()) {
            AppLog.stopCollecting()
        } else {
            AppLog.startCollecting(this)
        }
        updateStatus()
        if (isViewingFiles) {
            refreshFileList()
        }
    }

    private fun toggleViewMode() {
        isViewingFiles = !isViewingFiles
        if (isViewingFiles) {
            viewLogsBtn.text = "View Live Logs"
            liveLogsScroll.visibility = View.GONE
            fileListScroll.visibility = View.VISIBLE
            clearBtn.visibility = View.GONE
            refreshFileList()
        } else {
            viewLogsBtn.text = "View Logs File"
            liveLogsScroll.visibility = View.VISIBLE
            fileListScroll.visibility = View.GONE
            clearBtn.visibility = View.VISIBLE
            liveLogsTextView.text = ""
            for (line in AppLog.getLiveLogs()) {
                liveLogsTextView.append(line + "\n")
            }
            liveLogsScroll.post {
                liveLogsScroll.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    private fun clearScreen() {
        if (!isViewingFiles) {
            AppLog.clearLiveLogs()
            liveLogsTextView.text = ""
        }
    }

    private fun refreshFileList() {
        fileListContainer.removeAllViews()
        val dir = AppLog.getCollectedLogsDir(this)
        val files = dir.listFiles()?.sortedByDescending { FileDate -> FileDate.lastModified() } ?: emptyList()

        val textColorPrimary = getThemeColor(android.R.attr.textColorPrimary, Color.WHITE)
        val textColorSecondary = getThemeColor(android.R.attr.textColorSecondary, Color.GRAY)

        if (files.isEmpty()) {
            val emptyTv = TextView(this).apply {
                text = "No collected log files found.\nClick \"Start Collecting\" to record logs."
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding(0, 48, 0, 0)
                setTextColor(textColorSecondary)
            }
            fileListContainer.addView(emptyTv)
            return
        }

        for (file in files) {
            val fileRow = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 16
                }
                background = createRoundedDrawable(
                    if (isColorDark(textColorPrimary)) Color.parseColor("#F0F0F0") else Color.parseColor("#252525"),
                    dpToPx(12f).toFloat()
                )
            }

            val infoLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 16
                }
            }

            val nameTv = TextView(this).apply {
                text = file.name
                textSize = 15f
                setTypeface(null, Typeface.BOLD)
                setTextColor(textColorPrimary)
            }
            infoLayout.addView(nameTv)

            val sizeKb = file.length() / 1024f
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(file.lastModified()))
            val detailsTv = TextView(this).apply {
                text = "Size: %.2f KB  |  Modified: %s".format(sizeKb, dateStr)
                textSize = 12f
                setTextColor(textColorSecondary)
                setPadding(0, 4, 0, 0)
            }
            infoLayout.addView(detailsTv)
            fileRow.addView(infoLayout)

            val actionsLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.END
            }

            val actionBtnParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dpToPx(36f)
            ).apply {
                leftMargin = 12
            }

            val viewBtn = Button(this).apply {
                text = "View"
                textSize = 12f
                layoutParams = actionBtnParams
                setPadding(dpToPx(16f), 0, dpToPx(16f), 0)
                setAllCaps(false)
                setTextColor(textColorPrimary)
                background = createRoundedDrawable(
                    if (isColorDark(textColorPrimary)) Color.parseColor("#DCDCDC") else Color.parseColor("#383838"),
                    dpToPx(6f).toFloat()
                )
                setOnClickListener { showFileContentDialog(file) }
            }
            actionsLayout.addView(viewBtn)

            val exportBtn = Button(this).apply {
                text = "Export"
                textSize = 12f
                layoutParams = actionBtnParams
                setPadding(dpToPx(16f), 0, dpToPx(16f), 0)
                setAllCaps(false)
                setTextColor(textColorPrimary)
                background = createRoundedDrawable(
                    if (isColorDark(textColorPrimary)) Color.parseColor("#DCDCDC") else Color.parseColor("#383838"),
                    dpToPx(6f).toFloat()
                )
                setOnClickListener { exportLogFile(file) }
            }
            actionsLayout.addView(exportBtn)

            val deleteBtn = Button(this).apply {
                text = "Delete"
                textSize = 12f
                layoutParams = actionBtnParams
                setPadding(dpToPx(16f), 0, dpToPx(16f), 0)
                setAllCaps(false)
                setTextColor(Color.WHITE)
                background = createRoundedDrawable(
                    Color.parseColor("#E53935"),
                    dpToPx(6f).toFloat()
                )
                setOnClickListener { deleteLogFile(file) }
            }
            actionsLayout.addView(deleteBtn)

            fileRow.addView(actionsLayout)
            fileListContainer.addView(fileRow)
        }
    }

    private fun showFileContentDialog(file: File) {
        val textColorPrimary = getThemeColor(android.R.attr.textColorPrimary, Color.WHITE)
        val bgColor = getThemeColor(android.R.attr.windowBackground, Color.BLACK)

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 400f, resources.displayMetrics).toInt()
            )
            background = createRoundedDrawable(
                if (isColorDark(bgColor)) Color.parseColor("#1A1A1A") else Color.parseColor("#F5F5F5"),
                dpToPx(8f).toFloat()
            )
            setPadding(16, 16, 16, 16)
        }

        val textView = TextView(this).apply {
            text = try {
                file.readText()
            } catch (e: Exception) {
                "Failed to read file: ${e.message}"
            }
            textSize = 11f
            setTypeface(Typeface.MONOSPACE)
            setTextColor(textColorPrimary)
        }
        scrollView.addView(textView)
        container.addView(scrollView)

        AlertDialog.Builder(this)
            .setTitle(file.name)
            .setView(container)
            .setPositiveButton("Close", null)
            .setNeutralButton("Export") { _, _ -> exportLogFile(file) }
            .show()
    }

    private fun exportLogFile(file: File) {
        try {
            val authority = "${packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(this, authority, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Export Log File"))
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to export: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteLogFile(file: File) {
        AlertDialog.Builder(this)
            .setTitle("Delete File?")
            .setMessage("Are you sure you want to delete ${file.name}?")
            .setPositiveButton("Delete") { _, _ ->
                if (file.delete()) {
                    Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT).show()
                    refreshFileList()
                } else {
                    Toast.makeText(this, "Failed to delete file", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
