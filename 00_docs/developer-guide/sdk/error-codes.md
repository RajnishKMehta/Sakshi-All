# Error Codes Reference

Every failure from the SDK includes a `SakshiErrorCode` enum. This guide details each error code, when it happens, and how to recover.

### `VAULT_NOT_INSTALLED`
* **Retryable:** False
* **Description:** The system cannot resolve the Vault intent, either because Vault is not installed, disabled, or the client app is missing the required `<queries>` block in `AndroidManifest.xml`.
* **Action:** Prompt the user to install the Vault, or check your manifest configuration.

### `SERVICE_UNAVAILABLE`
* **Retryable:** True
* **Description:** Binding timed out or the connection could not be established due to current system constraints.
* **Action:** Retry the operation. If persistent, check device state or clear memory.

### `SERVICE_DISCONNECTED`
* **Retryable:** True
* **Description:** Connection to Vault was lost during an active request, usually because the Vault process crashed or was killed by the Android OS.
* **Action:** Re-issue the request if it is safe to do so. Note that non-idempotent operations may need state verification first.

### `PERMISSION_DENIED`
* **Retryable:** False
* **Description:** Android blocked the bind request due to security constraints.
* **Action:** Ensure correct Android intent setup and check for any strict background execution restrictions.

### `RECORDING_NOT_FOUND`
* **Retryable:** False
* **Description:** The operation targeted a `fileId` that does not exist in the Vault's database.
* **Action:** Refresh the client catalog. Do not retry with the same ID.

### `INVALID_PAYLOAD`
* **Retryable:** False
* **Description:** The arguments passed to the SDK were invalid (e.g. malformed extensions, empty strings).
* **Action:** Fix request arguments before calling the SDK.

### `DUPLICATE_FILE_ID`
* **Retryable:** False
* **Description:** The provided `fileId` is already in use by another file in the Vault.
* **Action:** Generate a new unique file ID (e.g. a UUID) and attempt the operation again.

### `SOURCE_FILE_INACCESSIBLE`
* **Retryable:** False
* **Description:** The source file URI provided by the client is missing, unreadable, or permission was denied to open it.
* **Action:** Verify the source file actually exists on disk and your application has read permission before asking the Vault to copy it.

### `IPC_ERROR`
* **Retryable:** True
* **Description:** A general or unclassified error occurring during the Binder transaction or reported by the Vault.
* **Action:** Log `diagnosticDetails` and retry if the operation is safe to repeat.

### `UNKNOWN_ERROR`
* **Retryable:** False
* **Description:** An unclassified exception occurred within the SDK or parsing the response failed.
* **Action:** Inspect `diagnosticDetails` or check for SDK updates.
