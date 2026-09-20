package rajnishkmehta.sakshi.camera.ui

import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import rajnishkmehta.sakshi.camera.R

/**
 * When in an activity where the status bar is hidden, the window layoutInDisplayCutoutMode
 * is set to [WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES], and a
 * material alert dialog is present that is large enough, the layout of the dialog will appear
 * broken and sometimes will shift randomly. These extensions force the dialog window to ignore
 * the short edges mode so that it will appear as normal.
 */

fun AlertDialog.ignoreShortEdges() {
    window?.attributes?.layoutInDisplayCutoutMode =
        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
}

fun AlertDialog.showIgnoringShortEdgeMode(): AlertDialog {
    ignoreShortEdges()
    show()
    return this
}

fun MaterialAlertDialogBuilder.showIgnoringShortEdgeMode(): AlertDialog =
    this.create().showIgnoringShortEdgeMode()


fun Context.showCustomMessageDialog(
    iconResId: Int,
    message: String,
    buttonText: String? = null
) {
    val view = LayoutInflater.from(this).inflate(R.layout.custom_message_dialog, null)
    val dialogIcon = view.findViewById<ImageView>(R.id.dialog_icon)
    val dialogMessage = view.findViewById<TextView>(R.id.dialog_message)
    val dialogButton = view.findViewById<Button>(R.id.dialog_button)

    dialogIcon.setImageResource(iconResId)
    dialogMessage.text = message
    if (buttonText != null) {
        dialogButton.text = buttonText
    }

    val dialog = MaterialAlertDialogBuilder(this).setView(view).create()
    dialogButton.setOnClickListener { dialog.dismiss() }
    dialog.showIgnoringShortEdgeMode()
}
