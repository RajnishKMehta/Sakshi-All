/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.camera.vault

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import rajnishkmehta.sakshi.sdk.api.SakshiClient
import rajnishkmehta.sakshi.sdk.api.SakshiClientConfig
import rajnishkmehta.sakshi.sdk.api.SakshiError

/**
 * ViewModel for handling Vault application discovery and selection.
 * Manages the state of the application list and performs compatibility verification.
 */
class VaultSelectionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppDiscoveryRepository(application)
    private var allApps: List<AppInfo> = emptyList()

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            allApps = repository.getInstalledApplications()
            _uiState.value = UiState.Success(allApps)
        }
    }

    /**
     * Filters the currently loaded list of applications by the given [query].
     * Matches against both the application name and package name (case-insensitive).
     *
     * @param query The search query string.
     */
    fun filter(query: String) {
        val currentState = _uiState.value
        if (currentState is UiState.Success || currentState is UiState.Filtering) {
            val filtered = if (query.isEmpty()) {
                allApps
            } else {
                allApps.filter {
                    it.name.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
                }
            }
            _uiState.value = UiState.Filtering(filtered)
        }
    }

    /**
     * Verifies if the selected application is a compatible Vault by attempting to ping it
     * via the SDK.
     *
     * @param packageName The package name of the application to verify.
     * @param onResult Callback invoked with the result (success boolean and optional error message).
     */
    fun verifyVaultApp(packageName: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val config = SakshiClientConfig(
                vaultPackageName = packageName,
                connectionTimeoutMs = 5000L
            )
            val tempClient = SakshiClient.create(getApplication(), config)
            val result = tempClient.pingVault()
            tempClient.disconnect()

            if (result.isSuccess) {
                onResult(true, null)
            } else {
                val err = result.errorOrNull()
                val message = err?.message ?: "Unknown error"
                onResult(false, message)
            }
        }
    }

    /**
     * Represents the UI state for the Vault application list.
     */
    sealed class UiState {
        object Loading : UiState()
        data class Success(val apps: List<AppInfo>) : UiState()
        data class Filtering(val apps: List<AppInfo>) : UiState()
    }
}
