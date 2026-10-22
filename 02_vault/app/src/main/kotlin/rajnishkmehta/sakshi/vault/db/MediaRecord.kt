/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Represents a media file managed by the Sakshi Vault.
 * This entity stores metadata, synchronization status, and deletion scheduling for media files.
 *
 * @property state Current lifecycle state, one of the [MediaState] values.
 * @property deletionScheduledAt Wall-clock time (epoch milliseconds) at which deletion was scheduled,
 * or `null` if the file is not scheduled for deletion.
 * @property deletionScheduledElapsedRealtime `SystemClock.elapsedRealtime()` captured together with
 * [deletionScheduledAt]. Used to detect wall-clock changes while the device has not rebooted.
 * @property deletionScheduledBootCount Device boot count captured together with [deletionScheduledAt],
 * or `null` if it was unavailable.
 */
@Entity(tableName = "media_records")
data class MediaRecord(
    @PrimaryKey
    val fileId: String,
    val originalUri: String,
    val vaultPath: String?,
    val mediaType: String, // "PHOTO", "VIDEO", "AUDIO", or "OTHER"
    val fileExtension: String,
    val state: String,
    val lastCopiedOffset: Long,
    val createdTime: Long,
    val updatedTime: Long,
    val deletionScheduledAt: Long? = null,
    val deletionScheduledElapsedRealtime: Long? = null,
    val deletionScheduledBootCount: Int? = null
)
