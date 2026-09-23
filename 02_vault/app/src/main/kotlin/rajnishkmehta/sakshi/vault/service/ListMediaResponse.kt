package rajnishkmehta.sakshi.vault.service

import kotlinx.serialization.Serializable

@Serializable
data class MediaItem(
    val fileId: String,
    val timestamp: Long
)
