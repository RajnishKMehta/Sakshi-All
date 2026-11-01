/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.sdk.internal.ipc

import android.os.DeadObjectException
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import rajnishkmehta.sakshi.sdk.api.SakshiError

public class ErrorMapperTest {

    @Test
    public fun `fromThrowable maps DeadObjectException to ServiceDisconnected`() {
        val ex = mockk<DeadObjectException>(relaxed = true)
        val error = ErrorMapper.fromThrowable(ex, "Default message")
        assertTrue(error is SakshiError.ServiceDisconnected)
        assertEquals("01_SERVICE_DISCONNECTED", error.errorCode)
    }

    @Test
    public fun `fromThrowable maps SecurityException to PermissionDenied`() {
        val ex = SecurityException("Permission denied")
        val error = ErrorMapper.fromThrowable(ex, "Default message")
        assertTrue(error is SakshiError.PermissionDenied)
        assertEquals("Default message", error.message)
        assertEquals("Permission denied", error.diagnosticDetails)
        assertEquals("03_PERMISSION_DENIED", error.errorCode)
    }

    @Test
    public fun `fromErrorMessage maps unique constraint to DuplicateFileId`() {
        val message = "UNIQUE constraint failed: file_id. File ID 'test-123' already exists."
        val error = ErrorMapper.fromErrorMessage(message)
        assertTrue(error is SakshiError.DuplicateFileId)
        val dupError = error as SakshiError.DuplicateFileId
        assertEquals("test-123", dupError.fileId)
        assertEquals("02_DUPLICATE_FILE_ID", error.errorCode)
    }

    @Test
    public fun `fromErrorMessage maps file not found to SourceFileInaccessible`() {
        val message = "open failed: ENOENT (No such file or directory)"
        val error = ErrorMapper.fromErrorMessage(message)
        assertTrue(error is SakshiError.SourceFileInaccessible)
        assertEquals(message, error.diagnosticDetails)
        assertEquals("03_SOURCE_FILE_INACCESSIBLE", error.errorCode)
    }

    @Test
    public fun `fromErrorMessage maps unknown errors to IpcError`() {
        val message = "NullPointerException at com.vault.some.Service"
        val error = ErrorMapper.fromErrorMessage(message, "Vault operation failed")
        assertTrue(error is SakshiError.IpcError)
        assertEquals("Vault operation failed", error.message)
        assertEquals(message, error.diagnosticDetails)
        assertEquals("04_IPC_ERROR", error.errorCode)
    }
}
