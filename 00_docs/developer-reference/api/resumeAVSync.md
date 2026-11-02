# resumeAVSync

## Purpose

Requests the Vault to resume a previously paused audio/video synchronization.

## Declaration

```kotlin
public suspend fun resumeAVSync(fileId: String): SakshiResult<Unit>
```

## Behaviour

Signals the Vault to resume synchronization of the specified file ID.

## Client Application Integration

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiClient

suspend fun resumeSync(client: SakshiClient, fileId: String) {
    val result = client.resumeAVSync(fileId)

    if (result.isSuccess) {
        println("Sync resumed.")
    } else {
        println("Failed to resume sync: ${result.errorOrNull()?.message}")
    }
}
```

## Vault Integration
The request is delegated to the scheduler to restart the internal sync job.
