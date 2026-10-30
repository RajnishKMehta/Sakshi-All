# stopAVSync

## Purpose

Requests the Vault to stop an active audio/video synchronization for a specific file ID.

## Declaration

```kotlin
public suspend fun stopAVSync(fileId: String): SakshiResult<CopyDoneAck>
```

## Behaviour

The SDK signals the Vault to terminate the ongoing sync loop for the provided file ID. The Vault will perform a final incremental copy to sync trailing bytes before finalizing the process.

## Client Application Integration

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiClient

suspend fun finalizeVideo(client: SakshiClient, fileId: String) {
    val result = client.stopAVSync(fileId)

    if (result.isSuccess) {
        println("Sync stopped and finalized.")
    } else {
        println("Failed to stop sync: ${result.errorOrNull()?.message}")
    }
}
```

## Vault Integration

`SakshiVaultServiceBinder` receives the request and delegates it to the sync scheduler. The scheduler cancels the active sync loop and schedules a final incremental copy. See [Shared Vault Integration](vault-integration.md).

## Errors and Limitations
*   Requires a valid `fileId`.
