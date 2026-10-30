# recoverMedia

## Purpose

Recovers a specific media file (or batch of files) previously scheduled for deletion in Vault.

## Declaration

```kotlin
// Single file
public suspend fun recoverMedia(fileId: String): SakshiResult<Unit>

// Batch files
public suspend fun recoverMedia(fileIds: List<String>): SakshiResult<BatchMediaRecoverResponse>
```

## Behaviour

Restores the file(s) to a valid state, preventing permanent deletion and making them visible in `listMedia` again.

## Client Application Integration

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiClient

suspend fun recoverDeletedFiles(client: SakshiClient, selectedIds: List<String>) {
    val result = client.recoverMedia(selectedIds)

    if (result.isSuccess) {
        val response = result.getOrNull()!!
        println("Successfully recovered: ${response.successfulIds.size} files")
    }
}
```

## Vault Integration
Delegates to the `RecoveryManager`. Requires the file state to transition from `DELETE` to `COMPLETED` and clears all scheduling timestamps (as per project rules).
