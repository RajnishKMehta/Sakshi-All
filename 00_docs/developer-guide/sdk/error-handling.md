# SDK Error Handling

The Sākṣī SDK leverages Kotlin's sealed classes to represent the result of operations safely, avoiding uncontrolled exceptions. Asynchronous API calls return a `SakshiResult<T>`, encapsulating either a successful result containing data of type `T`, or a failure containing a specific `SakshiError`.

## The `SakshiResult` Wrapper

The `SakshiResult<T>` wrapper makes error handling explicit. Developers must evaluate the result state.

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiClient
import rajnishkmehta.sakshi.sdk.api.SakshiResult
import rajnishkmehta.sakshi.sdk.api.SakshiError

suspend fun attemptDelete(client: SakshiClient, fileId: String) {
    val result = client.deleteMedia(fileId)

    when (result) {
        is SakshiResult.Success -> {
            println("Successfully scheduled deletion for $fileId")
        }
        is SakshiResult.Failure -> {
            handleError(result.error)
        }
    }
}
```

## `SakshiError` Types

The `SakshiError` sealed class hierarchy represents typed errors that occur during SDK operation or IPC communication. Because it inherits from `Throwable`, it can also be logged or thrown if necessary, but it is typically handled through flow control.

### Setup and Connection Errors

These errors typically occur before a request successfully reaches the Vault.

*   **`VaultNotInstalled`**: The Android system cannot resolve the Vault service intent. This means the Vault app is either not installed, disabled, or the client app is missing the required `<queries>` block in its manifest.
    *   *Resolution*: Instruct the user to install the Vault, or check the client manifest.
*   **`ServiceUnavailable`**: The client attempted to bind, but the connection timed out, or binding failed due to system limitations.
    *   *Resolution*: Retry the operation later.
*   **`ServiceDisconnected`**: The connection to the Vault was lost unexpectedly while a request was active, usually because the Vault process crashed or was killed by the OS.
    *   *Resolution*: The SDK will attempt to reconnect on the next request. For interrupted operations, client apps must decide if it is safe to retry.
*   **`PermissionDenied`**: The system blocked the bind request due to security constraints.
    *   *Resolution*: Ensure correct Android intent and manifest setup.

### Request Execution Errors

These errors are usually returned from the Vault itself after the request reaches it, serialized across the IPC boundary by `VaultResponder.sendError()`.

*   **`RecordingNotFound`**: The requested operation targets a `fileId` that does not exist in the Vault's database.
    *   *Properties*: Exposes the `fileId` that was not found.
    *   *Resolution*: Refresh the client catalog or handle the missing file.
*   **`InvalidPayload`**: The parameters passed to the SDK were invalid or malformed (e.g., empty `fileId`, unresolvable URI, unsupported `mediaType`).
    *   *Resolution*: Fix the arguments passed to the SDK.

### IPC and General Errors

*   **`IpcError`**: A generalized error occurring during the Binder transaction or reported by the Vault. This can wrap remote exceptions.
    *   *Properties*: May contain an integer `code` if provided by the Vault.
*   **`Unknown`**: A fallback error for unexpected failures, unclassified exceptions, or errors parsing the SDK response.

## Error Handling Strategies

1.  **Check for Success**: Use `result.isSuccess` or `result.isFailure` for simple checks.
2.  **Safe Extraction**: Use `result.getOrNull()` to extract the data gracefully, handling nulls if the operation failed.
3.  **Pattern Matching**: Use a `when` block to provide specific UI feedback based on the exact subclass of `SakshiError`.
