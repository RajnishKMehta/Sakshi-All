# listDeletedMedia

## Purpose

Lists all stored media files scheduled for deletion grouped by media type in JSON format.

## Declaration

```kotlin
public suspend fun listDeletedMedia(): SakshiResult<String>
```

## Behaviour

Retrieves a JSON catalog of media files whose state is currently `DELETE`. Normally listable media are excluded.

## Client Application Integration
Similar to `listMedia`, this is used to populate a 'Trash' or 'Recently Deleted' view.

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiClient

suspend fun fetchDeletedCatalog(client: SakshiClient) {
    val result = client.listDeletedMedia()

    if (result.isSuccess) {
        println("Deleted media catalog: ${result.getOrNull()}")
    }
}
```

## Vault Integration
Queries the `VaultDatabase` for records specifically in the `MediaState.DELETE` state. Formats the output based on `deletionScheduledAt` time rather than creation time.
