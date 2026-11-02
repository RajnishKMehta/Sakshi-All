# Data Models

The following primary data models are used by the SDK and IPC interfaces to transfer request and response data between clients (like Camera or Portal) and the Vault.

## Requests

### `FileCopyRequest`

Represents a standard file copy payload request submitted to the Vault.

| Property | Type | Description | Constraints |
| :--- | :--- | :--- | :--- |
| `fileId` | `String` | Unique identifier for this file submission. | 8-128 chars. Lowercase a-z, 0-9, `_`, `-`. |
| `uri` | `Uri` | Content Uri or file reference pointing to the source file. | |
| `mediaType` | `String` | Type classification of the file. | Expected values: `"PHOTO"`, `"VIDEO"`, `"AUDIO"`, `"OTHER"`. |
| `fileExtension` | `String` | The file extension without leading dot (e.g., `jpg`, `mp4`). | 1-12 chars. No spaces, slashes, or `..`. |
| `timestampEpochMs` | `Long` | Epoch timestamp in ms when the file was captured. | Default: `System.currentTimeMillis()` |
| `metadata` | `Map<String, String>`| Optional key-value metadata. | Default: `emptyMap()` |

*Note: For image transfers from Sākṣī Camera, `mediaType` is standardly set to `"OTHER"`.*

### `AVSyncRequest`

Payload sent to initiate incremental audio/video synchronization (crash-safe streaming).

| Property | Type | Description | Constraints |
| :--- | :--- | :--- | :--- |
| `fileId` | `String` | Unique identifier generated for the recording. | 8-128 chars. Lowercase a-z, 0-9, `_`, `-`. |
| `uri` | `Uri` | Content Uri or file reference pointing to the active recording file. | |
| `mediaType` | `String` | Type classification of the file. | Expected values: `"PHOTO"`, `"VIDEO"`, `"AUDIO"`, `"OTHER"`. |
| `fileExtension` | `String` | The file extension (e.g., `mp4`). | 1-12 chars. No spaces, slashes, or `..`. |
| `startTimestampEpochMs`| `Long` | Epoch timestamp in ms when recording started. | Default: `System.currentTimeMillis()` |
| `metadata` | `Map<String, String>`| Optional key-value metadata. | Default: `emptyMap()` |

*Note: For video streams from Sākṣī Camera, `mediaType` is standardly set to `"AUDIO"`.*

## Responses

### `CopyDoneAck`

Returned by `copyFile` and `stopAVSync` to acknowledge successful file finalization in the Vault.

| Property | Type | Description |
| :--- | :--- | :--- |
| `fileId` | `String` | The ID of the successfully copied file. |
| `bytesCopied` | `Long` | Total number of bytes finalized in Vault storage. |

### `BatchMediaDeleteResponse`

Result of a batch deletion request.

| Property | Type | Description |
| :--- | :--- | :--- |
| `successfulIds` | `List<String>` | File IDs that were successfully scheduled for deletion. |
| `failedIds` | `Map<String, String>` | Map of file IDs to error strings for files that failed deletion. |

### `BatchMediaRecoverResponse`

Result of a batch recovery request.

| Property | Type | Description |
| :--- | :--- | :--- |
| `successfulIds` | `List<String>` | File IDs that were successfully recovered. |
| `failedIds` | `Map<String, String>` | Map of file IDs to error strings for files that failed recovery. |
