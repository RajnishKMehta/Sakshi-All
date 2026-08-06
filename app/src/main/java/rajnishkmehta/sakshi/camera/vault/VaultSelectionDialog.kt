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
import kotlinx.coroutines.launch
import rajnishkmehta.sakshi.camera.CamConfig
import rajnishkmehta.sakshi.camera.ui.activities.MainActivity
import rajnishkmehta.sakshi.camera.R

class VaultSelectionDialog : DialogFragment() {

    private val viewModel: VaultSelectionViewModel by viewModels()
    private lateinit var adapter: VaultAppAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView

    var isMandatory: Boolean = false
    private var backPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
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
        val dialog = object : Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                if (isMandatory) {
                    if (backPressedOnce) {
                        requireActivity().finish()
                    } else {
                        backPressedOnce = true
                        Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                        view?.postDelayed({ backPressedOnce = false }, 2000)
                    }
                } else {
                    super.onBackPressed()
                }
            }
        }
        dialog.setCanceledOnTouchOutside(!isMandatory)
        dialog.setCancelable(!isMandatory)
        return dialog
    }

    private fun onAppSelected(appInfo: AppInfo) {
        // Show loading/verifying state if needed
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        viewModel.verifyVaultApp(appInfo.packageName) { isCompatible ->
            if (!isAdded) return@verifyVaultApp
            if (isCompatible) {
                val camConfig = CamConfig(requireActivity() as MainActivity)
                camConfig.vaultPackage = appInfo.packageName
                Toast.makeText(requireContext(), "Vault updated successfully", Toast.LENGTH_SHORT).show()

                // If it's mandatory, we might need to notify MainActivity to refresh SakshiClient
                // A better approach would be to have an interface callback, but for simplicity we can dismiss
                // and let MainActivity recreate it if we use a callback or broadcast.
                // We'll use parentFragmentManager result API.
                parentFragmentManager.setFragmentResult("vault_selection", Bundle().apply {
                    putString("package_name", appInfo.packageName)
                })
                dismissAllowingStateLoss()
            } else {
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Selected app is not a compatible Sakshi Vault", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val TAG = "VaultSelectionDialog"
    }
}
