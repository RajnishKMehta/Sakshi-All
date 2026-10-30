# getMedia

## Purpose

Retrieves the details of a specific media file, including its `ContentResolver` URI. This is necessary for a client application to actually read the media data (e.g., to display an image or play a video) from the Vault's secure storage using Android's standard Content Provider mechanism.

## Declaration

```kotlin
public suspend fun getMedia(fileId: String): SakshiResult<MediaDetailsResponse>
```

## Behaviour

The SDK queries the Vault for a specific `fileId`. If the file exists, it returns a `MediaDetailsResponse` containing metadata and a constructed `contentUri`. The client application can then use this URI with an Android `ContentResolver` to open an input stream to the file.

## Client Application Integration

A client application uses this when a user selects a specific item to view.

```kotlin
// Example inspired by Portal's MediaViewerViewModel
import rajnishkmehta.sakshi.sdk.api.SakshiClient

suspend fun viewMedia(client: SakshiClient, fileId: String) {
    val result = client.getMedia(fileId)

    if (result.isSuccess) {
        val details = result.getOrNull()!!
        val uriString = details.contentUri

        println("Media details retrieved.")
        println("File ID: ${details.fileId}")
        println("Content URI: $uriString")

        // Pass the uriString to a video player or image loader
    } else {
        println("Failed to retrieve media details: ${result.errorOrNull()?.message}")
    }
}
```

## Vault Integration

The Vault executes this query synchronously. It looks up the `fileId` in the database. If found, it constructs a content URI using the format `content://{packageName}.mediaprovider/media/{mediaType}/{fileId}` and returns it along with the file metadata.

## Errors and Limitations

*   **File Not Found**: If the provided `fileId` does not exist in the Vault's database, the SDK will return a `SakshiResult.Failure`.
