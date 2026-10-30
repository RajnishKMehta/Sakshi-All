# isAVSynced

## Purpose

Queries Vault to check the current synchronization progress and state for a specific file.

## Declaration

```kotlin
public suspend fun isAVSynced(fileId: String): SakshiResult<RecordingQueryResponse>
```

## Behaviour

Checks if the recording exists in the Vault and returns its current synchronization status (e.g., if it is completed, idle, or its current byte offset).

## Client Application Integration

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiClient

suspend fun checkSyncStatus(client: SakshiClient, fileId: String) {
    val result = client.isAVSynced(fileId)

    if (result.isSuccess) {
        val response = result.getOrNull()!!
        println("Exists: ${response.exists}")
        println("Is Completed: ${response.isCompleted}")
        println("Sync State: ${response.syncState}")
    }
}
```

## Vault Integration
Executed synchronously. The binder queries the Room database (`getRecord(fileId)`) and returns the state details.
