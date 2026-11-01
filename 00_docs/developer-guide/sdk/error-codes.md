# Error Codes Reference

Every failure from the SDK includes an `errorCode` string. This guide details standard error codes, when they happen, and how to recover. The Vault or SDK may return other codes in the future; unknown codes should be handled safely as standard failures.

### Standard Categories

Errors follow a two-digit prefix format to identify their category:
* **00**: Other or Unknown.
* **01**: Connection - Failures related to binding, finding, or communicating with the Vault service.
* **02**: Validation - Failures related to invalid arguments, missing files, or bad state in the request.
* **03**: Environment - Failures caused by system limitations like permissions or I/O access.
* **04**: System Fault - Unexpected system faults or remote exceptions.

### `01_VAULT_NOT_INSTALLED`
* **Category:** Connection (01)
* **Description:** The system cannot resolve the Vault intent, either because Vault is not installed, disabled, or the client app is missing the required `<queries>` block in `AndroidManifest.xml`.
* **Action:** Prompt the user to install the Vault, or check your manifest configuration.

### `01_SERVICE_UNAVAILABLE`
* **Category:** Connection (01)
* **Description:** Binding timed out or the connection could not be established due to current system constraints.
* **Action:** Retry the operation. If persistent, check device state or clear memory.

### `01_SERVICE_DISCONNECTED`
* **Category:** Connection (01)
* **Description:** Connection to Vault was lost during an active request, usually because the Vault process crashed or was killed by the Android OS.
* **Action:** Re-issue the request if it is safe to do so. Note that non-idempotent operations may need state verification first.

### `03_PERMISSION_DENIED`
* **Category:** Environment (03)
* **Description:** Android blocked the bind request due to security constraints.
* **Action:** Ensure correct Android intent setup and check for any strict background execution restrictions.

### `02_RECORDING_NOT_FOUND`
* **Category:** Validation (02)
* **Description:** The operation targeted a `fileId` that does not exist in the Vault's database.
* **Action:** Refresh the client catalog. Do not retry with the same ID.

### `02_INVALID_PAYLOAD`
* **Category:** Validation (02)
* **Description:** The arguments passed to the SDK were invalid (e.g. malformed extensions, empty strings).
* **Action:** Fix request arguments before calling the SDK.

### `02_DUPLICATE_FILE_ID`
* **Category:** Validation (02)
* **Description:** The provided `fileId` is already in use by another file in the Vault.
* **Action:** Generate a new unique file ID (e.g. a UUID) and attempt the operation again.

### `03_SOURCE_FILE_INACCESSIBLE`
* **Category:** Environment (03)
* **Description:** The source file URI provided by the client is missing, unreadable, or permission was denied to open it.
* **Action:** Verify the source file actually exists on disk and your application has read permission before asking the Vault to copy it.

### `04_IPC_ERROR`
* **Category:** System Fault (04)
* **Description:** A general or unclassified error occurring during the Binder transaction or reported by the Vault.
* **Action:** Log `diagnosticDetails` and retry if the operation is safe to repeat.

### `00_UNKNOWN_ERROR`
* **Category:** Other (00)
* **Description:** An unclassified exception occurred within the SDK or parsing the response failed.
* **Action:** Inspect `diagnosticDetails` or check for SDK updates.
