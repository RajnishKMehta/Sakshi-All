package rajnishkmehta.sakshi.camera.ui

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * A generic dialog fragment that presents a list of single-choice options.
 * Uses the Fragment Result API to pass the selected option's index back to the calling activity
 * or fragment. This ensures proper state retention across configuration changes.
 */
class OptionsSelectionDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        val titleResId = args.getInt(ARG_TITLE_RES_ID)
        val options = args.getStringArray(ARG_OPTIONS) ?: emptyArray()
        val selectedIndex = args.getInt(ARG_SELECTED_INDEX, -1)
        val requestKey = args.getString(ARG_REQUEST_KEY) ?: ""

        val builder = MaterialAlertDialogBuilder(requireContext())
        if (titleResId != 0) {
            builder.setTitle(titleResId)
        }
        builder.setSingleChoiceItems(options, selectedIndex) { dialog, which ->
            setFragmentResult(requestKey, bundleOf(RESULT_SELECTED_INDEX to which))
            dialog.dismiss()
        }
        return builder.create()
    }

    companion object {
        const val TAG = "OptionsSelectionDialog"
        private const val ARG_TITLE_RES_ID = "arg_title_res_id"
        private const val ARG_OPTIONS = "arg_options"
        private const val ARG_SELECTED_INDEX = "arg_selected_index"
        private const val ARG_REQUEST_KEY = "arg_request_key"

        const val RESULT_SELECTED_INDEX = "result_selected_index"

        fun newInstance(titleResId: Int, options: Array<String>, selectedIndex: Int, requestKey: String): OptionsSelectionDialog {
            val fragment = OptionsSelectionDialog()
            val b = Bundle()
            b.putInt(ARG_TITLE_RES_ID, titleResId)
            b.putStringArray(ARG_OPTIONS, options)
            b.putInt(ARG_SELECTED_INDEX, selectedIndex)
            b.putString(ARG_REQUEST_KEY, requestKey)
            fragment.arguments = b
            return fragment
        }
    }
}
