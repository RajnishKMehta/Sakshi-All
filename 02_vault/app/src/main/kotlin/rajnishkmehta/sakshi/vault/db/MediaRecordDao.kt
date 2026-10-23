/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update

/**
 * Data Access Object (DAO) for accessing and modifying [MediaRecord] database records.
 */
@Dao
interface MediaRecordDao {

    /**
     * Inserts a new [MediaRecord] into the database. If a record with the same [MediaRecord.fileId] already exists,
     * it will be replaced.
     *
     * @param record The [MediaRecord] to insert or replace.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MediaRecord)

    /**
     * Updates an existing [MediaRecord] in the database.
     *
     * @param record The [MediaRecord] with updated fields.
     */
    @Update
    suspend fun updateRecord(record: MediaRecord)

    /**
     * Retrieves a [MediaRecord] by its unique identifier.
     *
     * @param fileId The unique identifier of the record.
     * @return The matching [MediaRecord], or `null` if not found.
     */
    @Query("SELECT * FROM media_records WHERE fileId = :fileId")
    suspend fun getRecord(fileId: String): MediaRecord?

    /**
     * Retrieves all [MediaRecord]s stored in the database, ordered by their creation time in descending order.
     *
     * @return A list of all [MediaRecord]s.
     */
    @Query("SELECT * FROM media_records ORDER BY createdTime DESC")
    suspend fun getAllRecords(): List<MediaRecord>

    /**
     * Retrieves all [MediaRecord]s that match the specified [MediaState].
     *
     * @param state The [MediaState] string to filter by.
     * @return A list of matching [MediaRecord]s.
     */
    @Query("SELECT * FROM media_records WHERE state = :state")
    suspend fun getRecordsInState(state: String): List<MediaRecord>

    /**
     * Deletes a [MediaRecord] by its unique identifier.
     *
     * @param fileId The unique identifier of the record to delete.
     */
    @Query("DELETE FROM media_records WHERE fileId = :fileId")
    suspend fun deleteRecord(fileId: String)

    /**
     * Atomically moves a record to [MediaState.DELETE] if its current state is one of [allowedStates].
     *
     * The state check and the update run in a single SQL statement, so a concurrent state change
     * (for example a synchronization restarting) cannot be overwritten.
     *
     * @return the number of updated rows: 1 if the record was scheduled, 0 otherwise.
     */
    @Query(
        "UPDATE media_records SET state = '" + MediaState.DELETE + "', " +
            "deletionScheduledAt = :scheduledAt, " +
            "deletionScheduledElapsedRealtime = :scheduledElapsedRealtime, " +
            "deletionScheduledBootCount = :scheduledBootCount, " +
            "updatedTime = :scheduledAt " +
            "WHERE fileId = :fileId AND state IN (:allowedStates)"
    )
    suspend fun markScheduledForDeletion(
        fileId: String,
        scheduledAt: Long,
        scheduledElapsedRealtime: Long,
        scheduledBootCount: Int?,
        allowedStates: List<String>
    ): Int

    /**
     * Deletes a record only if it is still scheduled for deletion with the given [scheduledAt] timestamp.
     *
     * This guards against removing a record that was re-created or changed after the cleanup started.
     *
     * @return the number of deleted rows.
     */
    @Query(
        "DELETE FROM media_records WHERE fileId = :fileId AND state = '" + MediaState.DELETE + "' " +
            "AND deletionScheduledAt = :scheduledAt"
    )
    suspend fun deleteScheduledRecord(fileId: String, scheduledAt: Long): Int

    /**
     * Atomically recovers a record from [MediaState.DELETE] to [MediaState.COMPLETED]
     * and clears its deletion scheduling timestamps.
     *
     * @return the number of updated rows: 1 if the record was recovered, 0 otherwise.
     */
    @Query(
        "UPDATE media_records SET state = '" + MediaState.COMPLETED + "', " +
            "deletionScheduledAt = NULL, " +
            "deletionScheduledElapsedRealtime = NULL, " +
            "deletionScheduledBootCount = NULL " +
            "WHERE fileId = :fileId AND state = '" + MediaState.DELETE + "'"
    )
    suspend fun recoverScheduledRecord(fileId: String): Int
}
