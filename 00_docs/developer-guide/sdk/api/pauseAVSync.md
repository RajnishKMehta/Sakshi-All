# pauseAVSync

## Purpose

Requests the Vault to temporarily pause an active audio/video synchronization.

## Declaration

```kotlin
public suspend fun pauseAVSync(fileId: String): SakshiResult<Unit>
```

## Behaviour

Signals the Vault to suspend synchronization of the specified file ID.

## Client Application Integration

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiClient

suspend fun pauseSync(client: SakshiClient, fileId: String) {
    val result = client.pauseAVSync(fileId)

    if (result.isSuccess) {
        println("Sync paused.")
    } else {
        println("Failed to pause sync: ${result.errorOrNull()?.message}")
    }
}
```

## Vault Integration
The request is delegated to the scheduler to suspend the internal sync job.
