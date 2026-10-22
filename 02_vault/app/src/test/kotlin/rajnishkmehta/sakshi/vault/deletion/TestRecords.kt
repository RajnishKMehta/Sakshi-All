/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.deletion

import rajnishkmehta.sakshi.vault.db.MediaRecord
import rajnishkmehta.sakshi.vault.db.MediaState
import java.util.concurrent.TimeUnit

internal val THIRTY_DAYS: Long = TimeUnit.DAYS.toMillis(30)

internal fun testRecord(
    fileId: String = "photo_00000001",
    state: String = MediaState.COMPLETED,
    mediaType: String = "PHOTO",
    fileExtension: String = "jpg",
    vaultPath: String? = null,
    deletionScheduledAt: Long? = null,
    deletionScheduledElapsedRealtime: Long? = null,
    deletionScheduledBootCount: Int? = null
) = MediaRecord(
    fileId = fileId,
    originalUri = "content://camera/$fileId",
    vaultPath = vaultPath,
    mediaType = mediaType,
    fileExtension = fileExtension,
    state = state,
    lastCopiedOffset = 10L,
    createdTime = 1_000L,
    updatedTime = 1_000L,
    deletionScheduledAt = deletionScheduledAt,
    deletionScheduledElapsedRealtime = deletionScheduledElapsedRealtime,
    deletionScheduledBootCount = deletionScheduledBootCount
)
