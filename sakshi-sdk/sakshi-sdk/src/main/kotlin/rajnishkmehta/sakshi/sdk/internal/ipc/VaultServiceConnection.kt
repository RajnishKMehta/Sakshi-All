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

        val deferred = CompletableDeferred<ISakshiVaultService>()
        connectionDeferred = deferred

        val intent = Intent(config.vaultServiceAction).apply {
            setPackage(config.vaultPackageName)
        }

        val bound = runCatching {
            context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        }.getOrDefault(false)

        if (!bound) {
            connectionDeferred = null
            return SakshiResult.Failure(SakshiError.VaultNotInstalled())
        }

        val service = withTimeoutOrNull(config.connectionTimeoutMs) {
            deferred.await()
        }

        return if (service != null) {
            SakshiResult.Success(service)
        } else {
            unbindInternal()
            SakshiResult.Failure(SakshiError.ServiceUnavailable("Timed out connecting to Vault service"))
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
