package rajnishkmehta.sakshi.camera.vault

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import rajnishkmehta.sakshi.camera.CamConfig
import rajnishkmehta.sakshi.camera.ui.activities.MainActivity
import rajnishkmehta.sakshi.camera.R

class VaultSelectionDialog : BottomSheetDialogFragment() {

    private val viewModel: VaultSelectionViewModel by viewModels()
    private lateinit var adapter: VaultAppAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView

    var isMandatory: Boolean = false
    private var backPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_VaultSelection)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_vault_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progress_bar)
        recyclerView = view.findViewById(R.id.recycler_view)
        val searchBar: EditText = view.findViewById(R.id.search_bar)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = VaultAppAdapter { appInfo ->
            onAppSelected(appInfo)
        }
        recyclerView.adapter = adapter

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.filter(s?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is VaultSelectionViewModel.UiState.Loading -> {
                            progressBar.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        }
                        is VaultSelectionViewModel.UiState.Success -> {
                            progressBar.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                            adapter.submitList(state.apps)
                        }
                        is VaultSelectionViewModel.UiState.Filtering -> {
                            progressBar.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                            adapter.submitList(state.apps)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }

        if (isMandatory) {
            dialog.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (backPressedOnce) {
                        dismissAllowingStateLoss()
                    } else {
                        backPressedOnce = true
                        Toast.makeText(context, "Press back again to use Camera without Vault", Toast.LENGTH_SHORT).show()
                        lifecycleScope.launch {
                            kotlinx.coroutines.delay(2000)
                            backPressedOnce = false
                        }
                    }
                }
            })
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(false)
        }

        return dialog
    }

    private fun onAppSelected(appInfo: AppInfo) {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        viewModel.verifyVaultApp(appInfo.packageName) { isCompatible, errorMsg ->
            if (!isAdded) return@verifyVaultApp
            if (isCompatible) {
                Toast.makeText(requireContext(), "Vault updated successfully", Toast.LENGTH_SHORT).show()

                parentFragmentManager.setFragmentResult("vault_selection", Bundle().apply {
                    putString("package_name", appInfo.packageName)
                })
                dismissAllowingStateLoss()
            } else {
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Selected app is not compatible: ${errorMsg ?: "Unknown error"}", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val TAG = "VaultSelectionDialog"
    }
}
