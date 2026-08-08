package rajnishkmehta.sakshi.vault.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Data Access Object (DAO) for accessing and modifying [MediaRecord] database records.
 */
@Dao
interface MediaRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MediaRecord)

    @Update
    suspend fun updateRecord(record: MediaRecord)

    @Query("SELECT * FROM media_records WHERE fileId = :fileId")
    suspend fun getRecord(fileId: String): MediaRecord?

    @Query("SELECT * FROM media_records ORDER BY createdTime DESC")
    suspend fun getAllRecords(): List<MediaRecord>

    @Query("DELETE FROM media_records WHERE fileId = :fileId")
    suspend fun deleteRecord(fileId: String)
}
