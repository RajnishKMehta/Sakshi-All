/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.portal.ui.vault

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri


import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import rajnishkmehta.sakshi.portal.R
import rajnishkmehta.sakshi.portal.vault.AppInfo
import rajnishkmehta.sakshi.portal.vault.VaultSelectionViewModel
import rajnishkmehta.sakshi.portal.debug.DebugLogger as Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultSelectionScreen(
    viewModel: VaultSelectionViewModel = viewModel(),
    onAppSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        searchQuery = ""
        viewModel.filter("")
        try { Log.i(tag = "VaultSelection", message = "VaultSelectionScreen opened") } catch (e: Exception) {}
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.cancelVerification()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Vault App") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.cancelVerification()
                        onBackClick()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.vault_download_url)))
                        try {
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, context.getString(R.string.vault_download_error), Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, context.getString(R.string.vault_download_error), Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(
                            painter = painterResource(android.R.drawable.stat_sys_download),
                            contentDescription = "Download Vault"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.filter(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search apps...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is VaultSelectionViewModel.UiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is VaultSelectionViewModel.UiState.Success -> {
                        AppList(
                            apps = state.apps,
                            onAppClick = { app ->
                                if (!isVerifying) {
                                    isVerifying = true
                                    try { Log.i(tag = "VaultSelection", message = "Pinging selected app: ${app.packageName}") } catch (e: Exception) {}
                                    viewModel.verifyVaultApp(app.packageName) { isSuccess, error ->
                                        isVerifying = false
                                        if (isSuccess) {
                                            try { Log.i(tag = "VaultSelection", message = "Ping success for: ${app.packageName}") } catch (e: Exception) {}
                                            onAppSelected(app.packageName)
                                        } else {
                                            try { Log.e(tag = "VaultSelection", message = "Ping failed for ${app.packageName}: $error") } catch (e: Exception) {}
                                            Toast.makeText(context, "Failed to connect: $error", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }
                        )
                    }
                    is VaultSelectionViewModel.UiState.Filtering -> {
                        AppList(
                            apps = state.apps,
                            onAppClick = { app ->
                                if (!isVerifying) {
                                    isVerifying = true
                                    try { Log.i(tag = "VaultSelection", message = "Pinging selected app: ${app.packageName}") } catch (e: Exception) {}
                                    viewModel.verifyVaultApp(app.packageName) { isSuccess, error ->
                                        isVerifying = false
                                        if (isSuccess) {
                                            try { Log.i(tag = "VaultSelection", message = "Ping success for: ${app.packageName}") } catch (e: Exception) {}
                                            onAppSelected(app.packageName)
                                        } else {
                                            try { Log.e(tag = "VaultSelection", message = "Ping failed for ${app.packageName}: $error") } catch (e: Exception) {}
                                            Toast.makeText(context, "Failed to connect: $error", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                if (isVerifying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                            .clickable(enabled = false) {}
                    ) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
fun AppList(apps: List<AppInfo>, onAppClick: (AppInfo) -> Unit) {
    if (apps.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No apps found")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items = apps, key = { it.packageName }) { app ->
                AppItem(app = app, onClick = { onAppClick(app) })
            }
        }
    }
}

@Composable
fun AppItem(app: AppInfo, onClick: () -> Unit) {
    val bitmap = remember(app.icon) {
        // App icons might not have valid intrinsic dimensions if they are solid color drawables or misconfigured,
        // but typically launcher icons do. Providing fallback dimensions just in case.
        val width = if (app.icon.intrinsicWidth > 0) app.icon.intrinsicWidth else 144
        val height = if (app.icon.intrinsicHeight > 0) app.icon.intrinsicHeight else 144
        app.icon.toBitmap(width = width, height = height).asImageBitmap()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = "App Icon",
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = app.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = app.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
