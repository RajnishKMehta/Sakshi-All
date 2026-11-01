/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.sdk.internal.ipc

import android.os.DeadObjectException
import rajnishkmehta.sakshi.sdk.api.SakshiError

/**
 * Maps raw throwables and string messages into structured [SakshiError] instances.
 */
internal object ErrorMapper {

    /**
     * Maps a Throwable caught during an IPC call to the most appropriate [SakshiError].
     */
    internal fun fromThrowable(e: Throwable, defaultMessage: String): SakshiError {
        return when (e) {
            is DeadObjectException -> SakshiError.ServiceDisconnected(diagnosticDetails = e.message)
            is SecurityException -> SakshiError.PermissionDenied(message = defaultMessage, diagnosticDetails = e.message)
            is IllegalArgumentException -> SakshiError.InvalidPayload(message = defaultMessage, diagnosticDetails = e.message)
            else -> SakshiError.IpcError(message = defaultMessage, diagnosticDetails = e.message, cause = e)
        }
    }

    /**
     * Inspects a string error message returned from Vault to identify specific error states
     * that aren't natively codified by Vault's IPC response, providing structured errors.
     */
    internal fun fromErrorMessage(message: String, defaultMessage: String = message): SakshiError {
        val lowerMsg = message.lowercase()

        // Match Duplicate File ID
        if (lowerMsg.contains("unique constraint") || lowerMsg.contains("already exists") || lowerMsg.contains("duplicate")) {
            // Best effort extraction of fileId if present in the string (e.g. "File ID 'abc' already exists")
            val regex = "'([^']+)'".toRegex()
            val match = regex.find(message)
            val fileId = match?.groupValues?.get(1) ?: "unknown"
            return SakshiError.DuplicateFileId(fileId = fileId, diagnosticDetails = message)
        }

        // Match Source File Inaccessible
        if (lowerMsg.contains("file not found") || lowerMsg.contains("no such file") || lowerMsg.contains("enoent") || lowerMsg.contains("permission denied") && lowerMsg.contains("open")) {
             return SakshiError.SourceFileInaccessible(diagnosticDetails = message)
        }

        // Match Invalid Payload
        if (lowerMsg.contains("invalid path components") || lowerMsg.contains("invalid payload")) {
            return SakshiError.InvalidPayload(message = defaultMessage, diagnosticDetails = message)
        }

        return SakshiError.IpcError(message = defaultMessage, diagnosticDetails = message)
    }
}
