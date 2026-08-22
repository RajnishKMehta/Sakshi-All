package rajnishkmehta.sakshi.sdk.api

/**
 * Represents typed errors that can occur during Sakshi SDK operation or IPC communication.
 */
public sealed class SakshiError : Throwable() {

    /**
     * Error indicating that the Vault application is not installed on the Android device.
     *
     * @property message Description of error.
     */
    public data class VaultNotInstalled(
        override val message: String = "Vault application is not installed on this device."
    ) : SakshiError()

    /**
     * Error indicating that binding to the Vault service failed or the connection timed out.
     *
     * @property message Description of connection failure.
     */
    public data class ServiceUnavailable(
        override val message: String = "Vault service connection could not be established."
    ) : SakshiError()

    /**
     * Error indicating that the Vault service disconnected unexpectedly during an operation.
     *
     * @property message Description of service disconnection.
     */
    public data class ServiceDisconnected(
        override val message: String = "Vault service disconnected unexpectedly."
    ) : SakshiError()

    /**
     * Error indicating that the requested operation was rejected due to missing permissions.
     *
     * @property message Description of permission denial.
     */
    public data class PermissionDenied(
        override val message: String
    ) : SakshiError()

    /**
     * Error indicating that a recording file ID was not found in Vault.
     *
     * @property fileId The requested file ID that was missing.
     * @property message Description of missing recording.
     */
    public data class RecordingNotFound(
        public val fileId: String,
        override val message: String = "Recording with file ID '$fileId' was not found."
    ) : SakshiError()

    /**
     * Error indicating an invalid argument or payload passed to the SDK.
     *
     * @property message Description of payload invalidity.
     */
    public data class InvalidPayload(
        override val message: String
    ) : SakshiError()

    /**
     * Error indicating an IPC communication fault or Binder transaction exception.
     *
     * @property code Numeric error code if provided by Binder transaction.
     * @property message Description of IPC transaction error.
     * @property cause Optional underlying exception cause.
     */
    public data class IpcError(
        public val code: Int = -1,
        override val message: String,
        override val cause: Throwable? = null
    ) : SakshiError()

    /**
     * Fallback error for unexpected or unclassified failures.
     *
     * @property message Description of unknown failure.
     * @property cause Optional underlying exception cause.
     */
    public data class Unknown(
        override val message: String,
        override val cause: Throwable? = null
    ) : SakshiError()
}
