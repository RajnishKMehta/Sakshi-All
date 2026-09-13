package rajnishkmehta.sakshi.vault.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Represents a media file managed by the Sakshi Vault.
 * This entity stores metadata and synchronization status for both photos and videos.
 */
@Entity(tableName = "media_records")
data class MediaRecord(
    @PrimaryKey
    val fileId: String,
    val originalUri: String,
    val vaultPath: String?,
    val mediaType: String, // "PHOTO", "VIDEO", "AUDIO", or "OTHER"
    val fileExtension: String,
    val completionState: String, // "INITIALIZING", "SYNCING", "COMPLETED", "STOPPED", "FAILED"
    val lastCopiedOffset: Long,
    val createdTime: Long,
    val updatedTime: Long,
    val vaultAddedTimestamp: Long = System.currentTimeMillis()
)
