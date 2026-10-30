# getThumbnail

## Purpose

Retrieves the ContentProvider URI template for accessing media thumbnails.

## Declaration

```kotlin
public suspend fun getThumbnail(): SakshiResult<String>
```

## Behaviour

Queries the Vault for the expected URI template string used to load thumbnails.

## Client Application Integration

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiClient

suspend fun loadThumbnailTemplate(client: SakshiClient) {
    val result = client.getThumbnail()

    if (result.isSuccess) {
        val template = result.getOrNull()!!
        println("Thumbnail URI template: $template")
        // Use this template to construct final URIs in image loaders (e.g., Coil)
    }
}
```

## Vault Integration
The Vault simply returns the string template for its thumbnail ContentProvider.
