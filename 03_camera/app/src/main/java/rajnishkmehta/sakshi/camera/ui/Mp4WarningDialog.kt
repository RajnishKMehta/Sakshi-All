/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.camera.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import rajnishkmehta.sakshi.camera.R

/**
 * Warns the user before switching the video container format to normal MP4.
 *
 * Shows **Confirm** on the left and **Cancel** on the right. Confirm posts an empty
 * Fragment Result under [REQUEST_KEY]; Cancel, back, or tapping outside only dismisses
 * the dialog so the current setting stays unchanged.
 */
class Mp4WarningDialog : DialogFragment() {

    /**
     * Creates and returns the dialog for this fragment, utilizing a generic alert layout.
     *
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     * @return A new Dialog instance to be displayed by the Fragment.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_alert, null)

        view.findViewById<TextView>(R.id.dialog_message).setText(R.string.mp4_warning_message)

        val btnLeft = view.findViewById<Button>(R.id.dialog_button_left)
        btnLeft.setText(R.string.confirm)
        btnLeft.setOnClickListener {
            setFragmentResult(REQUEST_KEY, Bundle.EMPTY)
            dismiss()
        }

        val btnRight = view.findViewById<Button>(R.id.dialog_button_right)
        btnRight.setText(R.string.cancel)
        btnRight.setOnClickListener {
            dismiss()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.mp4_warning_title)
            .setView(view)
            .create()
    }

    /**
     * Companion object holding constant values related to [Mp4WarningDialog].
     */
    companion object {
        /** The tag used to identify this DialogFragment in the FragmentManager. */
        const val TAG = "Mp4WarningDialog"

        /** Fragment Result request key posted when the user confirms switching to normal MP4. */
        const val REQUEST_KEY = "mp4_warning_confirmed"
    }
}
