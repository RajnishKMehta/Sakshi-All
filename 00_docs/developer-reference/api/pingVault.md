# pingVault

## Purpose

Performs a health check against the Vault to verify connection and service availability. This is typically used to ensure the Vault is running and responsive before attempting more complex operations.

## Declaration

```kotlin
public suspend fun pingVault(): SakshiResult<VaultPingResponse>
```

## Behaviour

The SDK attempts to bind to the Vault service. If successful, it sends a basic ping request. It returns a success wrapper containing connection status and Vault version information, or a failure wrapper if the connection could not be established.

## Client Application Integration

A client application (like Portal or Camera) uses this method during initialization to verify readiness.

```kotlin
// Example from Portal's GalleryViewModel
import rajnishkmehta.sakshi.sdk.api.SakshiClient

suspend fun checkVaultHealth(client: SakshiClient) {
    val pingResult = client.pingVault()

    if (pingResult.isFailure) {
        val error = pingResult.errorOrNull()
        println("Vault is unavailable: ${error?.message}")
        // Update UI to show Vault unavailable state
    } else {
        val response = pingResult.getOrNull()
        println("Vault is healthy, version: ${response?.version}")
    }
}
```

## Vault Integration

See [Shared Vault Integration](vault-integration.md). The Vault service simply returns a bundle containing its internal version code and a success flag.

## Errors and Limitations

*   **Connection Timeout**: If the Vault package is not installed or the service is not running, the SDK will timeout based on the configured `connectionTimeoutMs` and return a failure.
