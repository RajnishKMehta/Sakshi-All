/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.portal.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
        val VAULT_PACKAGE_NAME_KEY = stringPreferencesKey("vault_package_name")
    }

    val useDynamicColorFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            // By default, Material You is enabled
            preferences[DYNAMIC_COLOR_KEY] ?: true
        }

    val vaultPackageNameFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[VAULT_PACKAGE_NAME_KEY]
        }

    suspend fun setUseDynamicColor(useDynamicColor: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = useDynamicColor
        }
    }

    suspend fun setVaultPackageName(packageName: String) {
        context.dataStore.edit { preferences ->
            preferences[VAULT_PACKAGE_NAME_KEY] = packageName
        }
    }
}
