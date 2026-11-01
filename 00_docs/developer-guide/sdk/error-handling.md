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

## `SakshiError` and `errorCode`

Every `SakshiError` provides a stable `errorCode` representing the specific failure reason, making it easy to handle programmatically.

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiErrorCode

fun handleError(error: SakshiError) {
    // 1. Programmatically identify failure
    if (error.errorCode == SakshiErrorCode.DUPLICATE_FILE_ID) {
        // e.g. generate a new ID and try again
    }
}
```

For a full list of SDK errors and practical resolution steps, refer to the [Error Codes](error-codes.md) reference.

## Extensibility

The SDK accepts any error code provided by the Vault. It does not force you to restrict logic to specific predefined constants. If an unrecognized integer code is returned by the Vault, it will be encapsulated properly inside a `SakshiError.IpcError`, with the original integer available in `IpcError.code` and `errorCode` set to `04_IPC_ERROR`.

## Diagnostics

The `diagnosticDetails` property contains safe internal details, such as raw string messages returned by the Vault or system exceptions. This should be logged for debugging but typically not shown directly to users.
