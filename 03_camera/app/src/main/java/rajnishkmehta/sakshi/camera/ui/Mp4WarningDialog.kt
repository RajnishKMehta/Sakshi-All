/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.camera.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
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
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_mp4_warning, null)

        view.findViewById<Button>(R.id.mp4_warning_confirm).setOnClickListener {
            setFragmentResult(REQUEST_KEY, Bundle.EMPTY)
            dismiss()
        }
        view.findViewById<Button>(R.id.mp4_warning_cancel).setOnClickListener {
            dismiss()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.mp4_warning_title)
            .setView(view)
            .create()
    }

    companion object {
        const val TAG = "Mp4WarningDialog"

        /** Fragment Result request key posted when the user confirms switching to normal MP4. */
        const val REQUEST_KEY = "mp4_warning_confirmed"
    }
}
