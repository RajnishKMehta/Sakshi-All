# copyFile

## Purpose

Sends a file payload to Vault for ingestion. This is used when a client application wants to securely store a completely recorded media file (like a photo or a finalized video) in the Vault.

## Declaration

```kotlin
public suspend fun copyFile(request: FileCopyRequest): SakshiResult<CopyDoneAck>
```

## Behaviour

The SDK constructs a request containing the source URI, a unique file ID, and metadata (like `mediaType` and `fileExtension`). It invokes the Vault service. The Vault schedules a worker to copy the file from the provided URI into its secure internal storage.

## Client Application Integration

A client application uses this to transfer files it has created or manages.

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiClient
import rajnishkmehta.sakshi.sdk.api.models.FileCopyRequest
import android.net.Uri

suspend fun savePhotoToVault(client: SakshiClient, photoUri: Uri, fileId: String) {
    val request = FileCopyRequest(
        fileId = fileId,
        uri = photoUri.toString(),
        mediaType = "OTHER", // Use "OTHER" for photos as per project conventions
        fileExtension = "jpg"
    )

    val result = client.copyFile(request)

    if (result.isSuccess) {
        println("File ingestion initiated successfully.")
    } else {
        println("Failed to initiate file copy: ${result.errorOrNull()?.message}")
    }
}
```

## Vault Integration

The `SakshiVaultServiceBinder` receives the request, validates the path components (using `PathValidator`), and delegates to a scheduler. The actual file copy operation is handled asynchronously by the Vault to avoid blocking the IPC thread. See [Shared Vault Integration](vault-integration.md).

## Errors and Limitations

*   **Validation Errors**: If the `fileId`, `mediaType`, or `fileExtension` contain invalid characters or path traversal sequences, the Vault will reject the request with an error.
*   **Missing URI**: An empty URI string will cause an immediate rejection.
