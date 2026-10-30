# Shared Vault Integration

All requests made through the `SakshiClient` ultimately route to the Vault application (`02_vault`). This document explains the common patterns used by the Vault to handle these requests.

## `SakshiVaultServiceBinder`

The core entry point on the Vault side is `SakshiVaultServiceBinder`, which implements the `ISakshiVaultService.Stub()` AIDL interface.

When a client application invokes a method on the SDK, the SDK constructs an IPC call to this binder. The Binder is responsible for:

1.  **Request Validation**: Checking for missing parameters (e.g., empty file IDs) or malformed requests.
2.  **Execution Scheduling**: Deciding whether to process the request synchronously using `runBlocking` (typically for database queries) or asynchronously by launching a coroutine within the `serviceScope`.
3.  **Response Construction**: Packaging the results (success, failure, data) into an Android `Bundle` to be sent back across the IPC boundary.

### Synchronous vs Asynchronous Handling

*   **Synchronous**: Methods that read data (e.g., `listMedia`, `isAVSynced`, `getMedia`) often execute synchronously using `runBlocking` to query the Room database. The resulting data is immediately packed into a `Bundle` and returned to the caller.
*   **Asynchronous (Callback-based)**: Methods that initiate long-running operations or write data (e.g., `startAVSync`) often accept an `ISakshiVaultCallback`. The Vault launches a coroutine on its `serviceScope` to handle the work and uses `VaultResponder` to asynchronously send status updates or completion acknowledgments back to the client.

## Shared Error Handling (`VaultResponder`)

For asynchronous methods utilizing callbacks, the Vault utilizes a shared `VaultResponder` utility. If validation fails (e.g., `PathValidator` throws an exception for invalid path components), or an error occurs during execution, the Vault uses `VaultResponder.sendError(callback, error)` to safely transmit a serialized `SakshiError` back to the SDK client.

This guarantees that unhandled exceptions on the Vault side do not crash the remote service but instead surface as structured failures within the `SakshiResult` returned by the client application's SDK call.
