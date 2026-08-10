package rajnishkmehta.sakshi.sdk.internal.ipc

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import rajnishkmehta.sakshi.sdk.api.SakshiClientConfig
import rajnishkmehta.sakshi.sdk.api.SakshiError
import rajnishkmehta.sakshi.sdk.api.SakshiResult

/**
 * Manages thread-safe Android ServiceConnection to Vault application.
 *
 * Encapsulates binding, unbinding, death recipient monitoring, and coroutine suspension
 * when acquiring the remote [ISakshiVaultService] binder proxy.
 */
internal class VaultServiceConnection(
    private val context: Context,
    private val config: SakshiClientConfig
) : ServiceConnection, IBinder.DeathRecipient {

    private val mutex: Mutex = Mutex()
    private var boundService: ISakshiVaultService? = null
    private var connectionDeferred: CompletableDeferred<ISakshiVaultService>? = null
    private var activeBinder: IBinder? = null

    /**
     * Obtains an active connection to [ISakshiVaultService], binding if necessary.
     *
     * @return [SakshiResult] containing the service proxy or [SakshiError].
     */
    internal suspend fun getService(): SakshiResult<ISakshiVaultService> = mutex.withLock {
        boundService?.let {
            if (it.asBinder().isBinderAlive) {
                return SakshiResult.Success(it)
            }
        }

        val pm = context.packageManager
        val isPackageInstalled = try {
            pm.getPackageInfo(config.vaultPackageName, 0)
            true
        } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
            false
        }

        if (!isPackageInstalled) {
            return SakshiResult.Failure(
                SakshiError.VaultNotInstalled("Vault application package '${config.vaultPackageName}' is not installed on this device.")
            )
        }

        val deferred = CompletableDeferred<ISakshiVaultService>()
        connectionDeferred = deferred

        val intent = Intent(config.vaultServiceAction).apply {
            setPackage(config.vaultPackageName)
        }

        val bound = try {
            context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        } catch (e: SecurityException) {
            connectionDeferred = null
            return SakshiResult.Failure(
                SakshiError.PermissionDenied("Permission denied binding to Vault package '${config.vaultPackageName}': ${e.message}")
            )
        } catch (e: Exception) {
            connectionDeferred = null
            return SakshiResult.Failure(
                SakshiError.ServiceUnavailable("Failed to bind to Vault package '${config.vaultPackageName}': ${e.message}")
            )
        }

        if (!bound) {
            connectionDeferred = null
            return SakshiResult.Failure(
                SakshiError.ServiceUnavailable("Vault service action '${config.vaultServiceAction}' in package '${config.vaultPackageName}' could not be bound.")
            )
        }

        val service = withTimeoutOrNull(config.connectionTimeoutMs) {
            deferred.await()
        }

        return if (service != null) {
            SakshiResult.Success(service)
        } else {
            unbindInternal()
            SakshiResult.Failure(
                SakshiError.ServiceUnavailable("Timed out (${config.connectionTimeoutMs}ms) connecting to Vault service '${config.vaultPackageName}'.")
            )
        }

    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if (service == null) {
            connectionDeferred?.completeExceptionally(IllegalStateException("Received null binder"))
            return
        }

        runCatching {
            service.linkToDeath(this, 0)
        }
        activeBinder = service

        val vaultService = ISakshiVaultService.Stub.asInterface(service)
        boundService = vaultService
        connectionDeferred?.complete(vaultService)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        clearServiceState()
    }

    override fun binderDied() {
        clearServiceState()
    }

    internal fun disconnect() {
        unbindInternal()
    }

    private fun clearServiceState() {
        activeBinder?.unlinkToDeath(this, 0)
        activeBinder = null
        boundService = null
        connectionDeferred?.cancel()
        connectionDeferred = null
    }

    private fun unbindInternal() {
        clearServiceState()
        runCatching {
            context.unbindService(this)
        }
    }
}
