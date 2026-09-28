/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.portal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import rajnishkmehta.sakshi.portal.ui.gallery.GalleryScreen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rajnishkmehta.sakshi.portal.ui.theme.PortalTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var useDynamicColor by remember { mutableStateOf(true) }
            var showSettingsDialog by remember { mutableStateOf(false) }

            PortalTheme(dynamicColor = useDynamicColor) {
                GalleryScreen(
                    onSettingsClick = { showSettingsDialog = true }
                )

                if (showSettingsDialog) {
                    AlertDialog(
                        onDismissRequest = { showSettingsDialog = false },
                        title = { Text("Settings") },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Use Material You (Dynamic Colors)")
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = useDynamicColor,
                                    onCheckedChange = { useDynamicColor = it }
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showSettingsDialog = false }) {
                                Text("Close")
                            }
                        }
                    )
                }
            }
        }
    }
}
