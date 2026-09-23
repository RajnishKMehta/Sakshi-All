package rajnishkmehta.sakshi.vault.service

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ListMediaResponseTest {

    @Test
    fun `test ListMedia serialization format`() {
        // Simulating the grouping that SakshiVaultRemoteService performs
        val simulatedGroups = mapOf(
            "video" to listOf(
                MediaItem(fileId = "vid_003", timestamp = 1790181123000),
                MediaItem(fileId = "vid_004", timestamp = 1790181125000)
            ),
            "photo" to listOf(
                MediaItem(fileId = "img_001", timestamp = 1790181124000)
            )
        )

        val jsonString = Json.encodeToString(simulatedGroups)

        // Assert json output structure matches expectations
        assertTrue(jsonString.contains("\"video\":["))
        assertTrue(jsonString.contains("\"photo\":["))
        assertTrue(jsonString.contains("\"fileId\":\"vid_003\""))
        assertTrue(jsonString.contains("\"timestamp\":1790181123000"))
    }
}
