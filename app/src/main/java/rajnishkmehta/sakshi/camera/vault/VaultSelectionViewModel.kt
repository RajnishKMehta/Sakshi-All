package rajnishkmehta.sakshi.camera.vault

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import rajnishkmehta.sakshi.sdk.api.SakshiClient
import rajnishkmehta.sakshi.sdk.api.SakshiClientConfig
import rajnishkmehta.sakshi.sdk.api.SakshiResult
import rajnishkmehta.sakshi.sdk.api.models.VaultPingResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

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

    fun verifyVaultApp(packageName: String, onResult: (SakshiResult<VaultPingResponse>) -> Unit) {
        viewModelScope.launch {
            val config = SakshiClientConfig(
                vaultPackageName = packageName,
                connectionTimeoutMs = 5000L
            )
            val tempClient = SakshiClient.create(getApplication(), config)
            val result = tempClient.pingVault()
            onResult(result)
        }
    }

    sealed class UiState {
        object Loading : UiState()
        data class Success(val apps: List<AppInfo>) : UiState()
        data class Filtering(val apps: List<AppInfo>) : UiState()
    }
}
