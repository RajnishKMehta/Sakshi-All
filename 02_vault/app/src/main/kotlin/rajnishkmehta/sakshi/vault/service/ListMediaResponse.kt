/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.service

import kotlinx.serialization.Serializable

/**
 * Represents a simplified media item for listing APIs over IPC.
 *
 * @property fileId Unique identifier of the media file.
 * @property timestamp Context-dependent timestamp (e.g., creation time or deletion scheduling time).
 */
@Serializable
data class MediaItem(
    val fileId: String,
    val timestamp: Long
)
