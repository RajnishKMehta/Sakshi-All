/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.portal.vault

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rajnishkmehta.sakshi.portal.debug.DebugLogger as Log

class AppDiscoveryRepository(private val context: Context) {

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
            .distinctBy { it.packageName }
            .sortedWith(
                compareBy<AppInfo> { it.packageName != "rajnishkmehta.sakshi.vault" } // Vault first
                    .thenBy { it.hasLauncherActivity } // Non-launcher (background) apps next
                    .thenBy { it.name.lowercase() }
            )
    }
}
