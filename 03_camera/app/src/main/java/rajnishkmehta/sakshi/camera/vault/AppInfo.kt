/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.camera.vault

/**
 * Data class representing an installed application on the device.
 *
 * @property name The human-readable name of the application.
 * @property packageName The application's unique package name.
 * @property hasLauncherActivity True if the application has a main launcher activity.
 */
data class AppInfo(
    val name: String,
    val packageName: String,
    val hasLauncherActivity: Boolean
)
