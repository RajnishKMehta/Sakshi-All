/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.sdk.api

/**
 * Standardized error codes for Sākṣī SDK operations.
 * These codes allow client applications to programmatically identify failures.
 *
 * Error codes are prefixed with a two-digit category number:
 * - 00: OTHER
 * - 01: CONNECTION
 * - 02: VALIDATION
 * - 03: ENVIRONMENT
 * - 04: SYSTEM_FAULT
 */
public object SakshiErrorCode {
    public const val VAULT_NOT_INSTALLED: String = "01_VAULT_NOT_INSTALLED"
    public const val SERVICE_UNAVAILABLE: String = "01_SERVICE_UNAVAILABLE"
    public const val SERVICE_DISCONNECTED: String = "01_SERVICE_DISCONNECTED"
    public const val RECORDING_NOT_FOUND: String = "02_RECORDING_NOT_FOUND"
    public const val INVALID_PAYLOAD: String = "02_INVALID_PAYLOAD"
    public const val DUPLICATE_FILE_ID: String = "02_DUPLICATE_FILE_ID"
    public const val PERMISSION_DENIED: String = "03_PERMISSION_DENIED"
    public const val SOURCE_FILE_INACCESSIBLE: String = "03_SOURCE_FILE_INACCESSIBLE"
    public const val IPC_ERROR: String = "04_IPC_ERROR"
    public const val UNKNOWN_ERROR: String = "00_UNKNOWN_ERROR"
}
