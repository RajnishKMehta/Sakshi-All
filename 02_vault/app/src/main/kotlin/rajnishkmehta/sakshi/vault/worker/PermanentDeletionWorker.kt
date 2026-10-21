/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import rajnishkmehta.sakshi.vault.AppLog as Log
import rajnishkmehta.sakshi.vault.db.VaultDatabase
import rajnishkmehta.sakshi.vault.storage.AppPrivateStorageManager
import rajnishkmehta.sakshi.vault.thumbnail.ThumbnailManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A persistent worker that scans the database for media records scheduled for deletion
 * and permanently deletes the files if 30 days have elapsed.
 */
class PermanentDeletionWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting PermanentDeletionWorker to clean up old scheduled files.")
        try {
            val db = VaultDatabase.getDatabase(context)
            val storageManager = AppPrivateStorageManager(context)
            val allRecords = db.mediaRecordDao().getAllRecords()
            val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
            val now = System.currentTimeMillis()
            var partialFailure = false

            for (record in allRecords) {
                val deletionTime = record.scheduledDeletionTime
                if (deletionTime != null && now - deletionTime >= thirtyDaysInMillis) {
                    Log.d(TAG, "Permanently deleting eligible file ${record.fileId}")
                    val deleted = storageManager.deleteMedia(record.fileId, record.mediaType, record.fileExtension)

                    // Attempt thumbnail deletion regardless of main file deletion success
                    val thumbnailDeleted = ThumbnailManager.deleteThumbnail(context, record.mediaType, record.fileId)

                    if (deleted) {
                        db.mediaRecordDao().deleteRecord(record.fileId)
                    } else {
                        Log.w(TAG, "Failed to delete physical file for ${record.fileId}. Will retry later.")
                        partialFailure = true
                    }
                }
            }

            if (partialFailure) {
                Result.retry()
            } else {
                Log.d(TAG, "PermanentDeletionWorker finished successfully.")
                Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in PermanentDeletionWorker", e)
            Result.retry()
        }
    }

    companion object {
        const val TAG = "PermanentDeletionWorker"
        const val WORK_NAME = "VaultPermanentDeletionWork"
    }
}
