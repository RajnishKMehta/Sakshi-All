/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.recovery

import kotlinx.coroutines.CancellationException
import rajnishkmehta.sakshi.sdk.api.validation.PathValidator
import rajnishkmehta.sakshi.vault.AppLog as Log
import rajnishkmehta.sakshi.vault.db.MediaRecordDao
import rajnishkmehta.sakshi.vault.db.MediaState
import rajnishkmehta.sakshi.vault.sync.MediaFileLocks

/**
 * Result of scheduling a batch recovery operation.
 */
data class BatchRecoveryResult(
    val successfulIds: List<String>,
    val failedIds: Map<String, String>
)

/**
 * Handles the recovery of media files that are scheduled for permanent deletion.
 *
 * This manager coordinates with [MediaRecordDao] to atomically move a file from
 * [MediaState.DELETE] back to [MediaState.COMPLETED] and clear its deletion timestamps.
 * It uses [MediaFileLocks] to ensure recovery does not race with the cleanup coordinator.
 */
class MediaRecoveryManager(
    private val dao: MediaRecordDao
) {
    private val tag = "MediaRecoveryManager"

    /**
     * Recovers a single media file.
     *
     * @param fileId The ID of the file to recover.
     * @return `true` if the file was successfully recovered, `false` otherwise.
     * @throws IllegalArgumentException if the [fileId] is invalid.
     */
    suspend fun recover(fileId: String): RecoveryResult {
        try {
            PathValidator.validateFileId(fileId)
        } catch (e: IllegalArgumentException) {
            return RecoveryResult.Rejected(fileId, "Invalid fileId: ${e.message}")
        }

        return MediaFileLocks.withFileLock(fileId) {
            try {
                val record = dao.getRecord(fileId)
                when {
                    record == null -> RecoveryResult.Rejected(fileId, "Media file not found")
                    record.state != MediaState.DELETE -> RecoveryResult.Rejected(fileId, "Media file is not scheduled for deletion")
                    else -> {
                        val updated = dao.recoverScheduledRecord(fileId)
                        if (updated == 1) {
                            Log.d(tag, "Successfully recovered media $fileId")
                            RecoveryResult.Recovered(fileId)
                        } else {
                            RecoveryResult.Rejected(fileId, "Media file changed while recovering; retry")
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(tag, "Database error while recovering $fileId", e)
                RecoveryResult.Rejected(fileId, "Database error while recovering")
            }
        }
    }

    /**
     * Recovers multiple media files. Each ID is processed independently; duplicates are collapsed.
     *
     * @param fileIds The list of file IDs to recover.
     * @return A [BatchRecoveryResult] detailing successful and failed recoveries.
     * @throws IllegalArgumentException if [fileIds] is empty.
     */
    suspend fun recoverBatch(fileIds: List<String>): BatchRecoveryResult {
        require(fileIds.isNotEmpty()) { "fileIds must not be empty" }
        val successful = mutableListOf<String>()
        val failed = linkedMapOf<String, String>()

        for (fileId in fileIds.distinct()) {
            when (val result = recover(fileId)) {
                is RecoveryResult.Recovered -> successful.add(fileId)
                is RecoveryResult.Rejected -> failed[fileId] = result.reason
            }
        }

        return BatchRecoveryResult(successful, failed)
    }

    sealed interface RecoveryResult {
        data class Recovered(val fileId: String) : RecoveryResult
        data class Rejected(val fileId: String, val reason: String) : RecoveryResult
    }
}
