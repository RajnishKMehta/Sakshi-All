/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.portal.ui.viewer

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rajnishkmehta.sakshi.portal.data.SettingsRepository
import rajnishkmehta.sakshi.sdk.api.SakshiClient
import rajnishkmehta.sakshi.sdk.api.SakshiClientConfig
import rajnishkmehta.sakshi.sdk.api.models.MediaDetailsResponse
import kotlinx.coroutines.flow.firstOrNull
import rajnishkmehta.sakshi.portal.debug.DebugLogger as Log

sealed interface MediaViewerUiState {
    data object Loading : MediaViewerUiState
    data class Success(val mediaDetails: MediaDetailsResponse) : MediaViewerUiState
    data class Error(val message: String) : MediaViewerUiState
}

sealed interface ExportUiState {
    data object Idle : ExportUiState
    data object Loading : ExportUiState
    data class Success(val destination: String) : ExportUiState
    data class Error(val message: String) : ExportUiState
}

class MediaViewerViewModel(
    application: Application,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<MediaViewerUiState>(MediaViewerUiState.Loading)
    val uiState: StateFlow<MediaViewerUiState> = _uiState.asStateFlow()

    private val _exportState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val exportState: StateFlow<ExportUiState> = _exportState.asStateFlow()

    private var sakshiClient: SakshiClient? = null

    fun loadMedia(fileId: String) {
        viewModelScope.launch {
            _uiState.value = MediaViewerUiState.Loading
            try {
                val vaultPackage = settingsRepository.vaultPackageFlow.firstOrNull() ?: "rajnishkmehta.sakshi.vault"

                val config = SakshiClientConfig(
                    vaultPackageName = vaultPackage,
                    connectionTimeoutMs = 5000L
                )

                sakshiClient?.disconnect()
                val client = SakshiClient.create(getApplication(), config)
                sakshiClient = client

                val result = client.getMedia(fileId)

                if (result.isSuccess) {
                    _uiState.value = MediaViewerUiState.Success(result.getOrNull()!!)
                } else {
                    _uiState.value = MediaViewerUiState.Error(result.errorOrNull()?.message ?: "Failed to load media")
                }
            } catch (e: Exception) {
                Log.e("MediaViewerViewModel", "Error loading media: ${e.message}")
                _uiState.value = MediaViewerUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun exportMedia(mediaDetails: MediaDetailsResponse) {
        if (_exportState.value is ExportUiState.Loading) return

        viewModelScope.launch {
            _exportState.value = ExportUiState.Loading
            try {
                withContext(Dispatchers.IO) {
                    val context = getApplication<Application>()
                    val resolver = context.contentResolver
                    val sourceUri = Uri.parse(mediaDetails.contentUri)

                    val mediaType = mediaDetails.mediaType.uppercase()
                    val (collection, relativePath) = when (mediaType) {
                        "PHOTO" -> MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) to Environment.DIRECTORY_PICTURES + "/Sakshi"
                        "VIDEO" -> MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) to Environment.DIRECTORY_MOVIES + "/Sakshi"
                        "AUDIO" -> MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) to Environment.DIRECTORY_MUSIC + "/Sakshi"
                        else -> MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) to Environment.DIRECTORY_DOWNLOADS + "/Sakshi"
                    }

                    val displayName = "${mediaDetails.fileId}.${mediaDetails.fileExtension}"

                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                        put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.MediaColumns.IS_PENDING, 1)
                        }
                    }

                    val destUri = resolver.insert(collection, values) ?: throw Exception("Failed to create MediaStore entry")

                    var success = false
                    try {
                        resolver.openInputStream(sourceUri)?.use { inputStream ->
                            resolver.openOutputStream(destUri)?.use { outputStream ->
                                inputStream.copyTo(outputStream)
                                success = true
                            }
                        }
                    } finally {
                        if (success) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val updateValues = ContentValues().apply {
                                    put(MediaStore.MediaColumns.IS_PENDING, 0)
                                }
                                resolver.update(destUri, updateValues, null, null)
                            }
                            _exportState.value = ExportUiState.Success(relativePath)
                        } else {
                            resolver.delete(destUri, null, null)
                            _exportState.value = ExportUiState.Error("Failed to stream file data")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MediaViewerViewModel", "Error exporting media: ${e.message}")
                _exportState.value = ExportUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetExportState() {
        _exportState.value = ExportUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        sakshiClient?.disconnect()
    }
}

class MediaViewerViewModelFactory(
    private val application: Application,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MediaViewerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MediaViewerViewModel(application, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
