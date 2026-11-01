/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.sdk.api

/**
 * Represents typed, structured errors that can occur during Sakshi SDK operation or IPC communication.
 *
 * @property errorCode The code identifying the specific failure condition.
 * @property message Human-readable description of the error.
 * @property diagnosticDetails Safe, internal details (like exception messages) for debugging, not intended for user UI.
 * @property cause Optional underlying exception cause.
 */
public sealed class SakshiError(
    public val errorCode: String,
    override val message: String,
    public open val diagnosticDetails: String? = null,
    override val cause: Throwable? = null
) : Throwable(message, cause) {

    /**
     * Error indicating that the Vault application is not installed on the Android device.
     */
    public data class VaultNotInstalled(
        override val message: String = "Vault application is not installed on this device.",
        override val diagnosticDetails: String? = null
    ) : SakshiError(SakshiErrorCode.VAULT_NOT_INSTALLED, message, diagnosticDetails)

    /**
     * Error indicating that binding to the Vault service failed or the connection timed out.
     */
    public data class ServiceUnavailable(
        override val message: String = "Vault service connection could not be established.",
        override val diagnosticDetails: String? = null
    ) : SakshiError(SakshiErrorCode.SERVICE_UNAVAILABLE, message, diagnosticDetails)

    /**
     * Error indicating that the Vault service disconnected unexpectedly during an operation.
     */
    public data class ServiceDisconnected(
        override val message: String = "Vault service disconnected unexpectedly.",
        override val diagnosticDetails: String? = null
    ) : SakshiError(SakshiErrorCode.SERVICE_DISCONNECTED, message, diagnosticDetails)

    /**
     * Error indicating that the requested operation was rejected due to missing permissions.
     */
    public data class PermissionDenied(
        override val message: String,
        override val diagnosticDetails: String? = null
    ) : SakshiError(SakshiErrorCode.PERMISSION_DENIED, message, diagnosticDetails)

    /**
     * Error indicating that a recording file ID was not found in Vault.
     *
     * @property fileId The requested file ID that was missing.
     */
    public data class RecordingNotFound(
        public val fileId: String,
        override val message: String = "Recording with file ID '$fileId' was not found.",
        override val diagnosticDetails: String? = null
    ) : SakshiError(SakshiErrorCode.RECORDING_NOT_FOUND, message, diagnosticDetails)

    /**
     * Error indicating an invalid argument or payload passed to the SDK.
     */
    public data class InvalidPayload(
        override val message: String,
        override val diagnosticDetails: String? = null
    ) : SakshiError(SakshiErrorCode.INVALID_PAYLOAD, message, diagnosticDetails)

    /**
     * Error indicating that the provided file ID is already used by another file in the Vault.
     *
     * @property fileId The duplicate file ID.
     * @property message Human-readable description of the error.
     * @property diagnosticDetails Safe, internal details (like exception messages) for debugging, not intended for user UI.
     */
    public data class DuplicateFileId(
        public val fileId: String,
        override val message: String = "File ID '$fileId' already exists.",
        override val diagnosticDetails: String? = null
    ) : SakshiError(SakshiErrorCode.DUPLICATE_FILE_ID, message, diagnosticDetails)

    /**
     * Error indicating that the source file URI provided is missing, unreadable, or inaccessible.
     */
    public data class SourceFileInaccessible(
        override val message: String = "Source file is inaccessible or missing.",
        override val diagnosticDetails: String? = null
    ) : SakshiError(SakshiErrorCode.SOURCE_FILE_INACCESSIBLE, message, diagnosticDetails)

    /**
     * Error indicating an IPC communication fault or Binder transaction exception.
     *
     * @property code Numeric error code if provided by Binder transaction.
     */
    public data class IpcError(
        public val code: Int = -1,
        override val message: String,
        override val cause: Throwable? = null,
        override val diagnosticDetails: String? = null
    ) : SakshiError(SakshiErrorCode.IPC_ERROR, message, diagnosticDetails, cause)

    /**
     * Fallback error for unexpected or unclassified failures.
     *
     * @property message Human-readable description of the error.
     * @property cause Optional underlying exception cause.
     * @property diagnosticDetails Safe, internal details (like exception messages) for debugging, not intended for user UI.
     * @property customErrorCode A custom string error code if provided.
     */
    public data class Unknown(
        override val message: String,
        override val cause: Throwable? = null,
        override val diagnosticDetails: String? = null,
        public val customErrorCode: String = SakshiErrorCode.UNKNOWN_ERROR
    ) : SakshiError(customErrorCode, message, diagnosticDetails, cause)
}
