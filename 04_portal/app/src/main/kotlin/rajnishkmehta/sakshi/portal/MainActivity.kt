/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.portal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import rajnishkmehta.sakshi.portal.data.SettingsRepository
import rajnishkmehta.sakshi.portal.ui.gallery.GalleryScreen
import rajnishkmehta.sakshi.portal.ui.settings.SettingsScreen
import rajnishkmehta.sakshi.portal.ui.theme.PortalTheme
import rajnishkmehta.sakshi.portal.ui.vault.VaultSelectionBottomSheet

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsRepository = SettingsRepository(applicationContext)

        setContent {
            val useDynamicColor by settingsRepository.useDynamicColorFlow.collectAsState(initial = true)
            val vaultPackageName by settingsRepository.vaultPackageNameFlow.collectAsState(initial = "")

            var currentScreen by remember { mutableStateOf(Screen.Gallery) }
            var showVaultSelection by remember { mutableStateOf(false) }

            val coroutineScope = rememberCoroutineScope()

            // If vaultPackageName is loaded and is null/empty, show selection
            if (vaultPackageName == null) {
                showVaultSelection = true
            }

            BackHandler(enabled = currentScreen != Screen.Gallery) {
                currentScreen = Screen.Gallery
            }

            PortalTheme(useDynamicColor = useDynamicColor) {
                when (currentScreen) {
                    Screen.Gallery -> {
                        GalleryScreen(
                            onSettingsClick = { currentScreen = Screen.Settings }
                        )
                    }
                    Screen.Settings -> {
                        SettingsScreen(
                            repository = settingsRepository,
                            onBackClick = { currentScreen = Screen.Gallery }
                        )
                    }
                }

                if (showVaultSelection) {
                    VaultSelectionBottomSheet(
                        onDismiss = {
                            // If they dismiss without selecting and we still have no vault, maybe just hide it or keep nagging?
                            // Let's just hide it for now
                            showVaultSelection = false
                        },
                        onVaultSelected = { packageName ->
                            coroutineScope.launch {
                                settingsRepository.setVaultPackageName(packageName)
                            }
                            showVaultSelection = false
                        }
                    )
                }
            }
        }
    }
}

enum class Screen {
    Gallery, Settings
}
