package rajnishkmehta.sakshi.camera.logging

import android.content.Intent
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

object DebugHelper {
    fun addLogsOption(activity: AppCompatActivity, layout: LinearLayout) {
        val textView = TextView(activity).apply {
            text = "Logs"
            textSize = 20f
            setPadding(16, 32, 16, 32)
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#444444"))
            setOnClickListener {
                activity.startActivity(Intent(activity, LogsActivity::class.java))
            }
        }
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 32)
        textView.layoutParams = params
        layout.addView(textView, 0)
    }
}
