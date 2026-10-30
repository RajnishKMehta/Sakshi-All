# SDK Connection Lifecycle

The Sākṣī SDK uses an automatic, lazy-binding connection lifecycle to communicate with the Vault over Android Inter-Process Communication (IPC). This section explains how the connection is established, managed, and released, which is crucial for client applications to manage resources effectively and handle connection failures gracefully.

## Architecture and the Service Connection

The connection is managed internally by `VaultServiceConnection`. It implements Android's `ServiceConnection` and `IBinder.DeathRecipient` interfaces.

It handles thread-safe connections, binding, unbinding, death recipient monitoring, and coroutine suspension when acquiring the remote `ISakshiVaultService` binder proxy.

## Binding Process

When a client application invokes an API on `SakshiClient` (e.g., `copyFile`, `startAVSync`), the SDK checks if a connection to the Vault already exists. If not, it automatically initiates the binding process.

1.  **Intent Construction**: The SDK creates an intent targeting the Vault's remote service using the package name and action defined in `SakshiClientConfig` (defaulting to `rajnishkmehta.sakshi.vault` and `rajnishkmehta.sakshi.vault.BIND_VAULT_SERVICE`).
2.  **`bindService` Call**: The SDK calls `Context.bindService()` using `Context.BIND_AUTO_CREATE`.
3.  **Suspension and Timeout**: The calling coroutine suspends until the `ServiceConnection` receives the `onServiceConnected` callback or the configured connection timeout (`connectionTimeoutMs`) expires. The default timeout is 5000ms.
4.  **Death Monitoring**: Once connected, the SDK registers a `DeathRecipient` on the binder to detect if the Vault application crashes or is forcibly stopped by the system.

## Connection States and Failures

During the binding process, several errors can occur, wrapped in a `SakshiResult.Failure`.

*   **`VaultNotInstalled`**: If `bindService` returns `false` (meaning the OS cannot find a service matching the intent), the SDK assumes the Vault app is not installed on the device, or the service is disabled/unavailable.
*   **`ServiceUnavailable`**: If the connection takes longer than `connectionTimeoutMs` to establish, or if an exception occurs during the bind process, the SDK fails the request with this error.
*   **`PermissionDenied`**: If a `SecurityException` is thrown during binding (typically indicating missing `<queries>` elements in the client's Manifest), it is surfaced as this error.

## Lifecycle Management and Unbinding

The connection remains active across multiple API calls, reducing the overhead of establishing a new connection for every request.

However, holding a connection to a background service consumes system resources. It is the responsibility of the client application to release the connection when it is no longer needed.

### Explicit Disconnection

Developers should explicitly disconnect when the SDK is no longer actively needed by calling `disconnect()`. This immediately unbinds the service and clears internal connection states.

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiClient
import androidx.lifecycle.ViewModel

class MyViewModel(private val client: SakshiClient) : ViewModel() {
    override fun onCleared() {
        super.onCleared()
        // Release the IPC connection to the Vault
        client.disconnect()
    }
}
```

### Unintended Disconnections

If the Vault service is killed by the system (e.g., due to low memory) or crashes, the `DeathRecipient` triggers. The SDK clears its internal connection state. The next API call initiated by the client will automatically attempt to bind to the Vault again.

There is no automatic retry for in-flight requests that are interrupted by a disconnection. These will fail with an appropriate `SakshiError` (such as an `IpcError`), and the client application must decide whether to retry the operation.
