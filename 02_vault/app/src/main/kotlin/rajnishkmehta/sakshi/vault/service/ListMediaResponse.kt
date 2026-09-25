/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.service

import kotlinx.serialization.Serializable

@Serializable
data class MediaItem(
    val fileId: String,
    val timestamp: Long
)
