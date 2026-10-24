/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.portal.vault

/**
 * Represents basic information about an installed application that could potentially serve as a Vault.
 *
 * @property name The human-readable application label.
 * @property packageName The application's unique package identifier.
 * @property hasLauncherActivity Whether the application has a visible launcher intent.
 */
data class AppInfo(
    val name: String,
    val packageName: String,
    val hasLauncherActivity: Boolean
)
