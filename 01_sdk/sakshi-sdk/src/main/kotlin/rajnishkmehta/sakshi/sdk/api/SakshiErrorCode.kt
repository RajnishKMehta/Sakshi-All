/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.sdk.api

/**
 * Standardized string error codes for Sākṣī SDK operations.
 *
 * The SDK accepts any error code string returned by the Vault. These constants
 * represent standard internal or common IPC errors that clients might explicitly handle.
 *
 * Error codes are prefixed with a two-digit category number:
 * - 00: OTHER
 * - 01: CONNECTION
 * - 02: VALIDATION
 * - 03: ENVIRONMENT
 * - 04: SYSTEM_FAULT
 */
public object SakshiErrorCode {
    /** Vault app is not installed, disabled, or missing manifest queries. */
    public const val VAULT_NOT_INSTALLED: String = "01_VAULT_NOT_INSTALLED"

    /** Service connection failed or timed out. */
    public const val SERVICE_UNAVAILABLE: String = "01_SERVICE_UNAVAILABLE"

    /** Vault service died or was killed during an active operation. */
    public const val SERVICE_DISCONNECTED: String = "01_SERVICE_DISCONNECTED"

    /** Operation requested a file ID that does not exist. */
    public const val RECORDING_NOT_FOUND: String = "02_RECORDING_NOT_FOUND"

    /** Client provided invalid or malformed arguments. */
    public const val INVALID_PAYLOAD: String = "02_INVALID_PAYLOAD"

    /** The provided file ID is already in use. */
    public const val DUPLICATE_FILE_ID: String = "02_DUPLICATE_FILE_ID"

    /** Android permission denied (e.g. binding rejected). */
    public const val PERMISSION_DENIED: String = "03_PERMISSION_DENIED"

    /** The source file URI provided by the client is inaccessible or missing. */
    public const val SOURCE_FILE_INACCESSIBLE: String = "03_SOURCE_FILE_INACCESSIBLE"

    /** General IPC transaction failure. */
    public const val IPC_ERROR: String = "04_IPC_ERROR"

    /** Unclassified or unexpected failure. */
    public const val UNKNOWN_ERROR: String = "00_UNKNOWN_ERROR"
}
