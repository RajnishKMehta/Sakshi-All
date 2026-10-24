/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.portal.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The DataStore instance used for persisting Portal settings.
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Repository for accessing and modifying user preferences via DataStore.
 */
class SettingsRepository(private val context: Context) {

    companion object {
        val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
        val VAULT_PACKAGE_KEY = stringPreferencesKey("vault_package")
        val APP_LOCK_ENABLED_KEY = booleanPreferencesKey("app_lock_enabled")
        val APP_LOCK_TIMEOUT_MS_KEY = longPreferencesKey("app_lock_timeout_ms")
        val APP_LOCK_SETUP_COMPLETED_KEY = booleanPreferencesKey("app_lock_setup_completed")
    }

    val useDynamicColorFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DYNAMIC_COLOR_KEY] ?: true
        }

    val vaultPackageFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[VAULT_PACKAGE_KEY]
        }

    val appLockEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[APP_LOCK_ENABLED_KEY] ?: false
        }


    val appLockTimeoutMsFlow: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[APP_LOCK_TIMEOUT_MS_KEY] ?: 60000L
        }

    val appLockSetupCompletedFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[APP_LOCK_SETUP_COMPLETED_KEY] ?: false
        }

    /**
     * Enables or disables dynamic coloring (Material You) for the application theme.
     *
     * @param useDynamicColor True to enable dynamic color, false otherwise.
     */
    suspend fun setUseDynamicColor(useDynamicColor: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = useDynamicColor
        }
    }

    /**
     * Sets the package name of the selected Vault application.
     *
     * @param packageName The package name to save, or null to clear it.
     */
    suspend fun setVaultPackage(packageName: String?) {
        context.dataStore.edit { preferences ->
            if (packageName == null) {
                preferences.remove(VAULT_PACKAGE_KEY)
            } else {
                preferences[VAULT_PACKAGE_KEY] = packageName
            }
        }
    }

    /**
     * Sets the package name of the Vault application only if one is not already selected.
     *
     * @param packageName The package name to set as default.
     */
    suspend fun setVaultPackageIfUnset(packageName: String) {
        context.dataStore.edit { preferences ->
            if (!preferences.contains(VAULT_PACKAGE_KEY)) {
                preferences[VAULT_PACKAGE_KEY] = packageName
            }
        }
    }

    /**
     * Enables or disables the app lock security feature.
     *
     * @param enabled True to enable app lock, false otherwise.
     */
    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[APP_LOCK_ENABLED_KEY] = enabled
        }
    }


    /**
     * Sets the timeout duration before the app locks itself after being backgrounded.
     *
     * @param timeoutMs The timeout duration in milliseconds.
     */
    suspend fun setAppLockTimeoutMs(timeoutMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[APP_LOCK_TIMEOUT_MS_KEY] = timeoutMs
        }
    }

    /**
     * Marks the initial app lock setup dialog as completed or pending.
     *
     * @param completed True if the user has completed or dismissed the setup, false otherwise.
     */
    suspend fun setAppLockSetupCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[APP_LOCK_SETUP_COMPLETED_KEY] = completed
        }
    }
}
