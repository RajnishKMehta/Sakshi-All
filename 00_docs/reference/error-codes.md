# Error Codes

The Sākṣī SDK uses structured error handling via `SakshiError` subclasses. These encapsulate error codes and descriptions returned either internally by the SDK or across IPC by the Vault backend.

Error codes are standard string constants defined in `SakshiErrorCode`. They are structured with a two-digit prefix denoting the category:

*   **00:** OTHER (Unclassified)
*   **01:** CONNECTION (Service binding/lifecycle)
*   **02:** VALIDATION (Client input/state)
*   **03:** ENVIRONMENT (System state/files)
*   **04:** SYSTEM_FAULT (IPC/Android OS failures)

## Connection Errors (01)

| Error Code | Description |
| :--- | :--- |
| `01_VAULT_NOT_INSTALLED` | The designated Vault application is not installed on the device, is disabled, or the calling app is missing the required `<queries>` declaration in its AndroidManifest.xml. |
| `01_SERVICE_UNAVAILABLE` | Service connection failed or timed out during bind. |
| `01_SERVICE_DISCONNECTED` | The Vault service died or was killed by the OS during an active operation. |

## Validation Errors (02)

| Error Code | Description |
| :--- | :--- |
| `02_RECORDING_NOT_FOUND` | The operation requested a `fileId` that does not exist in the Vault. |
| `02_INVALID_PAYLOAD` | The client provided invalid, malformed, or missing arguments in a request. |
| `02_DUPLICATE_FILE_ID` | A copy or sync request provided a `fileId` that already exists in the Vault. Duplicate file IDs are strictly rejected; they are not silently overwritten. |

## Environment Errors (03)

| Error Code | Description |
| :--- | :--- |
| `03_PERMISSION_DENIED` | Android permission denied (e.g., binding rejected due to missing signature permissions). |
| `03_SOURCE_FILE_INACCESSIBLE` | The source file URI provided by the client in a `FileCopyRequest` is inaccessible, unreadable, or missing. |

## System Faults (04)

| Error Code | Description |
| :--- | :--- |
| `04_IPC_ERROR` | A general IPC transaction failure occurred (e.g., `DeadObjectException`, `TransactionTooLargeException`). |

## Other Errors (00)

| Error Code | Description |
| :--- | :--- |
| `00_UNKNOWN_ERROR` | An unclassified or unexpected failure occurred. |
