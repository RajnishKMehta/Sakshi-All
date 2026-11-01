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

## `SakshiError` and `SakshiErrorCode`

Every `SakshiError` provides a stable `errorCode` representing the specific failure reason, making it easy to handle programmatically. You can inspect `errorCode.isRetryable` to determine if retrying the identical operation later might succeed. A `SakshiErrorCategory` property is also available to categorize the failure (e.g., `CONNECTION`, `VALIDATION`).

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiErrorCode

fun handleError(error: SakshiError) {
    // 1. Programmatically identify failure
    if (error.errorCode == SakshiErrorCode.DUPLICATE_FILE_ID) {
        // e.g. generate a new ID and try again
    }

    // 2. Or rely on retryability guidance
    if (error.errorCode.isRetryable) {
        // e.g. delay and retry
    } else {
        // notify user based on error.errorCode.recommendedAction
    }
}
```

## Error Categories (`SakshiErrorCategory`)

*   **`CONNECTION`**: Failures related to binding, finding, or communicating with the Vault service.
*   **`VALIDATION`**: Failures related to invalid arguments, missing files, or bad state in the request.
*   **`ENVIRONMENT`**: Failures caused by system limitations like permissions or I/O access.
*   **`SYSTEM_FAULT`**: Unexpected system faults or remote exceptions.

For a full list of SDK errors and practical resolution steps, refer to the [Error Codes](error-codes.md) reference.

## Diagnostics

The `diagnosticDetails` property contains safe internal details, such as raw string messages returned by the Vault or system exceptions. This should be logged for debugging but typically not shown directly to users.
