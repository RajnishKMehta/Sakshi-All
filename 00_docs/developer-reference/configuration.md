---
description: "Configuration settings used when instantiating `SakshiClient`."
icon: gear
---
# Configuration

Configuration settings used when instantiating `SakshiClient`.

```kotlin
data class SakshiClientConfig(
    val vaultPackageName: String = DEFAULT_VAULT_PACKAGE_NAME,
    val vaultServiceAction: String = DEFAULT_VAULT_SERVICE_ACTION,
    val connectionTimeoutMs: Long = DEFAULT_CONNECTION_TIMEOUT_MS
)
```

## Parameters

### `vaultPackageName`
Android package name of the Vault application.
*   **Default:** `"rajnishkmehta.sakshi.vault"`

### `vaultServiceAction`
Intent action used to bind to Vault's remote IPC service.
*   **Default:** `"rajnishkmehta.sakshi.vault.BIND_VAULT_SERVICE"`

### `connectionTimeoutMs`
Connection timeout in milliseconds when binding to Vault.
*   **Default:** `5000L`
