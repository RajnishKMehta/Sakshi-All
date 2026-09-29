/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.portal.ui.vault

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import rajnishkmehta.sakshi.portal.R
import rajnishkmehta.sakshi.portal.vault.AppInfo
import rajnishkmehta.sakshi.portal.vault.VaultSelectionViewModel
import rajnishkmehta.sakshi.portal.debug.DebugLogger as Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultSelectionBottomSheet(
    onDismiss: () -> Unit,
    onVaultSelected: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val viewModel: VaultSelectionViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Select Vault Application",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            var searchQuery by remember { mutableStateOf("") }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.filter(it)
                },
                label = { Text("Search Apps") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.vault_download_url)))
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Cannot open download link", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text("Download Sākṣī Vault")
            }

            when (val state = uiState) {
                is VaultSelectionViewModel.UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is VaultSelectionViewModel.UiState.Success -> {
                    AppList(apps = state.apps, viewModel = viewModel, onVaultSelected = onVaultSelected)
                }
                is VaultSelectionViewModel.UiState.Filtering -> {
                    AppList(apps = state.apps, viewModel = viewModel, onVaultSelected = onVaultSelected)
                }
            }
        }
    }
}

@Composable
fun AppList(
    apps: List<AppInfo>,
    viewModel: VaultSelectionViewModel,
    onVaultSelected: (String) -> Unit
) {
    var isVerifying by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (isVerifying) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(apps, key = { it.packageName }) { appInfo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            isVerifying = true
                            viewModel.verifyVaultApp(appInfo.packageName) { isCompatible, errorMsg ->
                                isVerifying = false
                                if (isCompatible) {
                                    Toast.makeText(context, "Vault connected successfully", Toast.LENGTH_SHORT).show()
                                    onVaultSelected(appInfo.packageName)
                                } else {
                                    Toast.makeText(context, "App is not compatible: ${errorMsg ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    appInfo.icon?.toBitmap()?.asImageBitmap()?.let {
                        Image(
                            bitmap = it,
                            contentDescription = appInfo.name,
                            modifier = Modifier.size(48.dp)
                        )
                    } ?: Spacer(modifier = Modifier.size(48.dp))

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(text = appInfo.name, style = MaterialTheme.typography.bodyLarge)
                        Text(text = appInfo.packageName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
