# SDK APIs

The Sākṣī SDK (`rajnishkmehta.sakshi.sdk`) exposes a suite of public asynchronous APIs for communicating with the Vault application via IPC.

The primary interface is `SakshiClient`. All public `suspend` functions return a `SakshiResult<T>`, encapsulating either a successful result (`SakshiResult.Success`) or a failure (`SakshiResult.Failure`).

## Initialization

The SDK client is created using a factory method. It requires an Android `Context` and configuration.

```kotlin
val client = SakshiClient.create(
    context = applicationContext,
    config = SakshiClientConfig(
        vaultPackageName = "rajnishkmehta.sakshi.vault"
    )
)
```

## Available Operations

### Connection Health

*   **`pingVault()`**
    *   **Description:** Performs a ping health check against Vault to verify connection and service availability.
    *   **Returns:** `SakshiResult<VaultPingResponse>`

### Standard File Transfer

*   **`copyFile(request: FileCopyRequest)`**
    *   **Description:** Initiates a standard, one-off file copy to Vault. Suitable for completed photos or standard videos.
    *   **Returns:** `SakshiResult<CopyDoneAck>`

### Crash-Safe AV Synchronization (Streaming)

These APIs manage streaming data to Vault while recording is ongoing, ensuring data is not lost if the camera app crashes.

*   **`startAVSync(request: AVSyncRequest)`**
    *   **Description:** Starts a crash-safe AV synchronization session (e.g., streaming fMP4). Returns a `Flow` that emits status updates.
    *   **Returns:** `Flow<SakshiResult<AVSyncStatus>>`
*   **`pauseAVSync(fileId: String)`**
    *   **Description:** Pauses an active AV sync session.
    *   **Returns:** `SakshiResult<Unit>`
*   **`resumeAVSync(fileId: String)`**
    *   **Description:** Resumes a paused AV sync session.
    *   **Returns:** `SakshiResult<Unit>`
*   **`stopAVSync(fileId: String)`**
    *   **Description:** Stops an active AV sync session, finalizing the file in Vault.
    *   **Returns:** `SakshiResult<CopyDoneAck>`
*   **`isAVSynced(fileId: String)`**
    *   **Description:** Checks if an ongoing AV sync session is correctly synchronized with Vault.
    *   **Returns:** `SakshiResult<RecordingQueryResponse>`

### Media Lifecycle Management

*   **`deleteMedia(fileId: String)`**
    *   **Description:** Schedules a single media file for deletion in Vault.
    *   **Returns:** `SakshiResult<Unit>`
*   **`deleteMedia(fileIds: List<String>)`**
    *   **Description:** Schedules multiple media files for deletion in Vault.
    *   **Returns:** `SakshiResult<BatchMediaDeleteResponse>`
*   **`recoverMedia(fileId: String)`**
    *   **Description:** Recovers a previously deleted media file (if it is still within the recovery window).
    *   **Returns:** `SakshiResult<Unit>`
*   **`recoverMedia(fileIds: List<String>)`**
    *   **Description:** Recovers multiple deleted media files.
    *   **Returns:** `SakshiResult<BatchMediaRecoverResponse>`

### Media Retrieval

*   **`getMedia(fileId: String)`**
    *   **Description:** Retrieves detailed information and temporary access to a specific media file.
    *   **Returns:** `SakshiResult<MediaDetailsResponse>`
*   **`listMedia()`**
    *   **Description:** Lists active media available in the Vault. (Returns a JSON string representation currently).
    *   **Returns:** `SakshiResult<String>`
*   **`listDeletedMedia()`**
    *   **Description:** Lists media scheduled for deletion in the Vault. (Returns a JSON string representation currently).
    *   **Returns:** `SakshiResult<String>`
*   **`getThumbnail()`**
    *   **Description:** (Deprecated or unimplemented placeholder based on interface).
    *   **Returns:** `SakshiResult<String>`

### Lifecycle

*   **`disconnect()`**
    *   **Description:** Disconnects the SDK from the Vault service.
    *   **Returns:** `Unit` (Standard function, not a suspend function returning `SakshiResult`).
