# listMedia

## Purpose

Lists all stored media files grouped by media type in JSON format. This method retrieves the current media catalog, explicitly excluding any media that is currently scheduled for deletion.

## Declaration

```kotlin
public suspend fun listMedia(): SakshiResult<String>
```

## Behaviour

The SDK queries the Vault for a list of available media. The result is returned as a raw JSON string representing the catalog structure.

## Client Application Integration

A client application, such as the Portal gallery, uses this to display available files to the user.

```kotlin
// Example inspired by Portal's GalleryViewModel
import rajnishkmehta.sakshi.sdk.api.SakshiClient

suspend fun fetchMediaList(client: SakshiClient) {
    val result = client.listMedia()

    if (result.isSuccess) {
        val jsonCatalog = result.getOrNull()
        println("Received media catalog: $jsonCatalog")
        // Parse JSON and update UI
    } else {
        println("Failed to fetch media list: ${result.errorOrNull()?.message}")
    }
}
```

## Vault Integration

Inside the Vault, `SakshiVaultServiceBinder` handles this request synchronously using `runBlocking`. It queries the `VaultDatabase` for all records, filters out records in the `DELETE` state, and formats the remaining records into a JSON string sorted by creation time before returning them in a bundle.

## Errors and Limitations

*   Returns a JSON string. The client application is responsible for parsing this string into appropriate data models.
