/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import rajnishkmehta.sakshi.vault.AppLog as Log
import rajnishkmehta.sakshi.vault.db.VaultDatabase
import rajnishkmehta.sakshi.vault.storage.AppPrivateStorageManager
import rajnishkmehta.sakshi.vault.storage.StorageManager
import rajnishkmehta.sakshi.vault.sync.CopyEngine
import rajnishkmehta.sakshi.vault.sync.SyncScheduler

/**
 * Remote IPC background service that implements the ISakshiVaultService AIDL interface.
 * It coordinates incoming photo and video synchronization requests from client applications
 * using the Sakshi SDK.
 *
 * This service is designed strictly as a background headless component with no launcher icon or UI.
 * All long-running operations are offloaded from Binder threads to a background coroutine scope
 * to ensure that client requests are non-blocking and the IPC layer remains highly responsive.
 */
class SakshiVaultRemoteService : Service() {

    private val tag = "SakshiVaultService"
    private var database: VaultDatabase? = null
    private var storageManager: StorageManager? = null
    private var copyEngine: CopyEngine? = null
    private var scheduler: SyncScheduler? = null
    private var serviceScope: CoroutineScope? = null
    private var binder: SakshiVaultServiceBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "SakshiVaultRemoteService onCreate")
        try {
            serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
            database = VaultDatabase.getDatabase(applicationContext)
            storageManager = AppPrivateStorageManager(applicationContext)
            copyEngine = CopyEngine(applicationContext, database!!, storageManager!!)
            scheduler = SyncScheduler(applicationContext, database!!, copyEngine!!)

            binder = SakshiVaultServiceBinder(
                applicationContext,
                database,
                copyEngine,
                scheduler,
                serviceScope
            )

            // Automatically resume any interrupted, pending synchronizations from database
            scheduler?.resumePendingSyncs()
            Log.d(tag, "SakshiVaultRemoteService onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(tag, "Error initializing SakshiVaultRemoteService in onCreate: ${e.message}", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(tag, "Client bound to service with intent: $intent")
        return binder
    }

    override fun onDestroy() {
        Log.d(tag, "SakshiVaultRemoteService onDestroy")
        try {
            scheduler?.cancelAll()
            serviceScope?.cancel()
        } catch (e: Exception) {
            Log.e(tag, "Error during onDestroy: ${e.message}", e)
        }
        super.onDestroy()
    }
}
