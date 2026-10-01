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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import android.graphics.drawable.Drawable
import androidx.compose.ui.geometry.Size

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    val context = LocalContext.current
    var iconDrawable by remember { mutableStateOf<Drawable?>(null) }

    LaunchedEffect(app.packageName) {
        withContext(Dispatchers.IO) {
            try {
                val pm = context.packageManager
                val icon = pm.getApplicationIcon(app.packageName)
                iconDrawable = icon
            } catch (e: Exception) {
                try { Log.e(tag = "VaultSelection", message = "Failed to load icon for ${app.packageName}: ${e.message}") } catch (logE: Exception) {}
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val defaultIcon = painterResource(id = android.R.drawable.sym_def_app_icon)

        // Custom Painter for Drawable
        val painter = remember(iconDrawable) {
            val d = iconDrawable
            if (d != null) {
                try {
                    object : Painter() {
                        override val intrinsicSize: Size
                            get() = Size(
                                d.intrinsicWidth.toFloat().takeIf { it > 0f } ?: 144f,
                                d.intrinsicHeight.toFloat().takeIf { it > 0f } ?: 144f
                            )

                        override fun DrawScope.onDraw() {
                            try {
                                drawIntoCanvas { canvas ->
                                    d.setBounds(0, 0, size.width.toInt(), size.height.toInt())
                                    d.draw(canvas.nativeCanvas)
                                }
                            } catch (e: Exception) {
                                try { Log.e(tag = "VaultSelection", message = "Error rendering icon for ${app.packageName}: ${e.message}") } catch (logE: Exception) {}
                            }
                        }
                    }
                } catch (e: Exception) {
                    try { Log.e(tag = "VaultSelection", message = "Error creating painter for ${app.packageName}: ${e.message}") } catch (logE: Exception) {}
                    null
                }
            } else null
        }

        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = "App Icon",
                modifier = Modifier.size(48.dp)
            )
        } else {
            Icon(
                painter = defaultIcon,
                contentDescription = "App Icon Placeholder",
                modifier = Modifier.size(48.dp),
                tint = Color.Unspecified
            )
        }

        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = app.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = app.packageName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
