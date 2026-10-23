/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.camera.vault

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository responsible for discovering installed applications on the device.
 * Used primarily to find potential Vault applications.
 */
class AppDiscoveryRepository(private val context: Context) {

    /**
     * Retrieves a list of all installed applications, excluding system apps.
     * Applications with launcher activities are prioritized or flagged differently.
     *
     * @return A list of [AppInfo] representing installed applications.
     */
    suspend fun getInstalledApplications(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager

        // Find all packages that have a launcher activity
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val launcherResolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val launcherPackages = launcherResolveInfos.map { it.activityInfo.packageName }.toSet()

        val allPackages = pm.getInstalledApplications(0)

        allPackages
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 } // Filter out system apps
            .map { appInfo ->
                AppInfo(
                    name = pm.getApplicationLabel(appInfo).toString(),
                    packageName = appInfo.packageName,
                    hasLauncherActivity = launcherPackages.contains(appInfo.packageName)
                )
            }
            .sortedWith(
                compareBy<AppInfo> { it.hasLauncherActivity } // false (0) comes before true (1) -> Non-launcher apps at the top
                    .thenBy { it.name.lowercase() }
            )
    }
}
