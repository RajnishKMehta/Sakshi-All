# startAVSync

## Purpose

Initiates audio/video synchronization for a recording in Vault. This returns a cold flow that emits real-time updates until synchronization completes, fails, or is stopped.

## Declaration

```kotlin
public fun startAVSync(request: AVSyncRequest): Flow<SakshiResult<AVSyncStatus>>
```

## Behaviour

The SDK sends a request to the Vault to start syncing an audio or video file. Instead of a single synchronous return, it returns a Kotlin `Flow` which continuously emits `AVSyncStatus` objects.

## Client Application Integration

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiClient
import rajnishkmehta.sakshi.sdk.api.models.AVSyncRequest
import kotlinx.coroutines.flow.collect

suspend fun syncVideo(client: SakshiClient, fileId: String, videoUri: String) {
    val request = AVSyncRequest(
        fileId = fileId,
        uri = videoUri,
        mediaType = "AUDIO", // Assuming it's an audio/video recording format mapped as AUDIO
        fileExtension = "mp4"
    )

    client.startAVSync(request).collect { result ->
        if (result.isSuccess) {
            val status = result.getOrNull()!!
            println("Sync status: ${status.state} - Progress: ${status.progressPercentage}%")
        } else {
            println("Sync error: ${result.errorOrNull()?.message}")
        }
    }
}
```

## Vault Integration

Vault handles this request asynchronously. The `SakshiVaultServiceBinder` receives the request and delegates it to a scheduler. As progress is made on transferring/syncing the file, the Vault uses `VaultResponder` and a callback interface (`ISakshiVaultCallback`) to emit status updates back to the client over IPC.

## Errors and Limitations
*   **Path Validation**: If `fileId`, `mediaType`, or `fileExtension` fail `PathValidator` checks, the sync will not start.
*   **Connection**: If the IPC connection drops, the Flow will emit a failure.
