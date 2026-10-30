# deleteMedia

## Purpose

Schedules one or more media files for deletion in the Vault. Vault does not delete files immediately; it transitions them to a hidden state and permanently deletes them after a 30-day retention period.

## Declaration

```kotlin
// Single file deletion
public suspend fun deleteMedia(fileId: String): SakshiResult<Unit>

// Batch file deletion
public suspend fun deleteMedia(fileIds: List<String>): SakshiResult<BatchMediaDeleteResponse>
```

## Behaviour

The SDK sends the deletion request for a single `fileId` or a list of `fileIds`. Once scheduled, the files are hidden from the standard `listMedia` catalog but become visible via `listDeletedMedia`.

## Client Application Integration

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiClient

suspend fun deleteMultipleFiles(client: SakshiClient, selectedIds: List<String>) {
    val result = client.deleteMedia(selectedIds)

    if (result.isSuccess) {
        val response = result.getOrNull()!!
        println("Successfully scheduled for deletion: ${response.successfulIds.size} files")

        if (response.failedIds.isNotEmpty()) {
            println("Failed to delete the following files:")
            response.failedIds.forEach { (id, reason) ->
                println("- $id: $reason")
            }
        }
    } else {
        println("Batch deletion request failed completely: ${result.errorOrNull()?.message}")
    }
}
```

## Vault Integration

The Vault utilizes a `DeletionScheduler`. For batch operations, it attempts to schedule each file individually. The Vault returns a bundle containing lists of successfully scheduled IDs and a map of IDs that failed along with their specific failure reasons.

## Errors and Limitations

*   **Invalid State**: Files that are currently synchronizing cannot be deleted and will be reported in the `failedIds` map.
*   **Already Scheduled**: Repeated requests for an already scheduled file succeed without extending the 30-day retention period.
