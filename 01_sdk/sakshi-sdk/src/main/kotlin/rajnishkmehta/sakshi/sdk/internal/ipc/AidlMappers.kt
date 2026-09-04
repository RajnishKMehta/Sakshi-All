package rajnishkmehta.sakshi.sdk.internal.ipc

import android.net.Uri
import android.os.Bundle
import rajnishkmehta.sakshi.sdk.api.SakshiError
import rajnishkmehta.sakshi.sdk.api.models.CopyDoneAck
import rajnishkmehta.sakshi.sdk.api.models.PhotoRequest
import rajnishkmehta.sakshi.sdk.api.models.RecordingQueryResponse
import rajnishkmehta.sakshi.sdk.api.models.VaultPingResponse
import rajnishkmehta.sakshi.sdk.api.models.AVSyncRequest
import rajnishkmehta.sakshi.sdk.api.models.AVSyncStatus

/**
 * Internal mapping utilities for serializing and deserializing IPC Bundles.
 */
internal object AidlMappers {

    // Bundle Keys - Photo
    private const val KEY_URI: String = "uri"
    private const val KEY_MIME_TYPE: String = "mime_type"
    private const val KEY_TIMESTAMP: String = "timestamp"
    private const val KEY_ORIGINAL_URI: String = "original_uri"

    // Bundle Keys - Video Sync & Copy Done
    private const val KEY_FILE_ID: String = "file_id"
    private const val KEY_SYNC_STATE: String = "sync_state"
    private const val KEY_OFFSET_BYTES: String = "offset_bytes"
    private const val KEY_TOTAL_BYTES: String = "total_bytes"
    private const val KEY_IS_COMPLETED: String = "is_completed"
    private const val KEY_MESSAGE: String = "message"

    // Bundle Keys - Ping / Error
    private const val KEY_IS_AVAILABLE: String = "is_available"
    private const val KEY_VAULT_VERSION: String = "vault_version"
    private const val KEY_RESPONSE_TIME_MS: String = "response_time_ms"
    private const val KEY_EXISTS: String = "exists"
    private const val KEY_ERROR_CODE: String = "error_code"

    internal fun toBundle(request: PhotoRequest): Bundle {
        return Bundle().apply {
            putString(KEY_FILE_ID, request.fileId)
            putString(KEY_URI, request.uri.toString())
            putString(KEY_MIME_TYPE, request.mimeType)
            putLong(KEY_TIMESTAMP, request.timestampEpochMs)
            for ((key, value) in request.metadata) {
                putString("meta_$key", value)
            }
        }
    }


    internal fun toBundle(request: AVSyncRequest): Bundle {
        return Bundle().apply {
            putString(KEY_FILE_ID, request.fileId)
            putString(KEY_URI, request.uri.toString())
            putString(KEY_MIME_TYPE, request.mimeType)
            putLong(KEY_TIMESTAMP, request.startTimestampEpochMs)
            for ((key, value) in request.metadata) {
                putString("meta_$key", value)
            }
        }
    }

    internal fun toAVSyncStatus(bundle: Bundle): AVSyncStatus {
        val fileId = bundle.getString(KEY_FILE_ID, "")
        val stateStr = bundle.getString(KEY_SYNC_STATE, AVSyncStatus.State.INITIALIZING.name)
        val state = runCatching { AVSyncStatus.State.valueOf(stateStr) }
            .getOrDefault(AVSyncStatus.State.INITIALIZING)
        val offset = bundle.getLong(KEY_OFFSET_BYTES, 0L)
        val total = bundle.getLong(KEY_TOTAL_BYTES, -1L)
        val isCompleted = bundle.getBoolean(KEY_IS_COMPLETED, false)
        val msg = bundle.getString(KEY_MESSAGE)

        return AVSyncStatus(
            fileId = fileId,
            state = state,
            lastCopiedOffsetBytes = offset,
            totalBytes = total,
            isCompleted = isCompleted,
            message = msg
        )
    }

    internal fun toBundle(status: AVSyncStatus): Bundle {
        return Bundle().apply {
            putString(KEY_FILE_ID, status.fileId)
            putString(KEY_SYNC_STATE, status.state.name)
            putLong(KEY_OFFSET_BYTES, status.lastCopiedOffsetBytes)
            putLong(KEY_TOTAL_BYTES, status.totalBytes)
            putBoolean(KEY_IS_COMPLETED, status.isCompleted)
            status.message?.let { putString(KEY_MESSAGE, it) }
        }
    }

    internal fun toBundle(ack: CopyDoneAck): Bundle {
        return Bundle().apply {
            putString(KEY_FILE_ID, ack.fileId)
            ack.originalUri?.let { putString(KEY_ORIGINAL_URI, it.toString()) }
            putLong(KEY_TOTAL_BYTES, ack.totalCopiedBytes)
            putLong(KEY_TIMESTAMP, ack.timestampEpochMs)
        }
    }

    internal fun toCopyDoneAck(bundle: Bundle): CopyDoneAck {
        val fileId = bundle.getString(KEY_FILE_ID, "")
        val originalUriStr = bundle.getString(KEY_ORIGINAL_URI)
        val totalCopied = bundle.getLong(KEY_TOTAL_BYTES, 0L)
        val timestamp = bundle.getLong(KEY_TIMESTAMP, System.currentTimeMillis())
        val originalUri = originalUriStr?.let { Uri.parse(it) }

        return CopyDoneAck(
            fileId = fileId,
            originalUri = originalUri,
            totalCopiedBytes = totalCopied,
            timestampEpochMs = timestamp
        )
    }

    internal fun toRecordingQueryResponse(fileId: String, bundle: Bundle): RecordingQueryResponse {
        val exists = bundle.getBoolean(KEY_EXISTS, false)
        val stateStr = bundle.getString(KEY_SYNC_STATE)
        val state = stateStr?.let {
            runCatching { AVSyncStatus.State.valueOf(it) }.getOrNull()
        }
        val offset = bundle.getLong(KEY_OFFSET_BYTES, 0L)
        val isCompleted = bundle.getBoolean(KEY_IS_COMPLETED, false)

        return RecordingQueryResponse(
            fileId = fileId,
            exists = exists,
            state = state,
            lastCopiedOffsetBytes = offset,
            isCompleted = isCompleted
        )
    }

    internal fun toVaultPingResponse(bundle: Bundle): VaultPingResponse {
        val isAvailable = bundle.getBoolean(KEY_IS_AVAILABLE, true)
        val version = bundle.getString(KEY_VAULT_VERSION, "1.0.0")
        val responseTime = bundle.getLong(KEY_RESPONSE_TIME_MS, 0L)
        val serverTimestamp = bundle.getLong(KEY_TIMESTAMP, System.currentTimeMillis())

        return VaultPingResponse(
            isAvailable = isAvailable,
            vaultVersion = version,
            responseTimeMs = responseTime,
            serverTimestampEpochMs = serverTimestamp
        )
    }

    internal fun toSakshiError(bundle: Bundle): SakshiError {
        val errorCode = bundle.getInt(KEY_ERROR_CODE, -1)
        val msg = bundle.getString(KEY_MESSAGE, "Vault IPC operation failed")
        val fileId = bundle.getString(KEY_FILE_ID)

        return when (errorCode) {
            1 -> SakshiError.PermissionDenied(msg)
            2 -> fileId?.let { SakshiError.RecordingNotFound(it, msg) } ?: SakshiError.InvalidPayload(msg)
            3 -> SakshiError.InvalidPayload(msg)
            else -> SakshiError.IpcError(code = errorCode, message = msg)
        }
    }

    internal fun toErrorBundle(error: SakshiError): Bundle {
        return Bundle().apply {
            val code = when (error) {
                is SakshiError.PermissionDenied -> 1
                is SakshiError.RecordingNotFound -> 2
                is SakshiError.InvalidPayload -> 3
                is SakshiError.IpcError -> error.code
                else -> -1
            }
            putInt(KEY_ERROR_CODE, code)
            putString(KEY_MESSAGE, error.message)
            if (error is SakshiError.RecordingNotFound) {
                putString(KEY_FILE_ID, error.fileId)
            }
        }
    }
}
