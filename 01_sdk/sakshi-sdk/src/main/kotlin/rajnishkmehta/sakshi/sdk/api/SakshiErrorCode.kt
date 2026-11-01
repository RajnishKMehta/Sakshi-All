/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.sdk.api

/**
 * Standardized error codes for Sākṣī SDK operations.
 * These codes allow client applications to programmatically identify failures
 * and determine appropriate recovery actions without parsing error messages.
 *
 * @property isRetryable Indicates if retrying the identical operation later might succeed.
 * @property recommendedAction Guidance for the client developer on how to handle the error.
 */
public enum class SakshiErrorCode(
    public val isRetryable: Boolean,
    public val recommendedAction: String
) {
    /** Vault app is not installed, disabled, or missing manifest queries. */
    VAULT_NOT_INSTALLED(false, "Prompt user to install Vault app."),

    /** Service connection failed or timed out. */
    SERVICE_UNAVAILABLE(true, "Retry operation. If persistent, check device state."),

    /** Vault service died or was killed during an active operation. */
    SERVICE_DISCONNECTED(true, "Re-issue the request if safe, or notify user."),

    /** Android permission denied (e.g. binding rejected). */
    PERMISSION_DENIED(false, "Check client manifest and Android intent setup."),

    /** Operation requested a file ID that does not exist. */
    RECORDING_NOT_FOUND(false, "Refresh client catalog. Do not retry with same ID."),

    /** Client provided invalid or malformed arguments. */
    INVALID_PAYLOAD(false, "Fix request arguments before retrying."),

    /** The provided file ID is already in use. */
    DUPLICATE_FILE_ID(false, "Generate a new unique file ID and retry."),

    /** The source file URI provided by the client is inaccessible or missing. */
    SOURCE_FILE_INACCESSIBLE(false, "Verify source file exists and client has read permission."),

    /** General IPC transaction failure. */
    IPC_ERROR(true, "Retry operation. Investigate if persistent."),

    /** Unclassified or unexpected failure. */
    UNKNOWN_ERROR(false, "Inspect diagnostic details or check for SDK updates.");
}
