/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.service

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import rajnishkmehta.sakshi.vault.db.MediaRecord
import rajnishkmehta.sakshi.vault.db.MediaState

/**
 * Returns the records exposed through `listMedia`: only [MediaState.LISTABLE_STATES].
 * Records that are still synchronizing or scheduled for deletion ([MediaState.DELETE]) are excluded.
 */
internal fun filterListableRecords(records: List<MediaRecord>): List<MediaRecord> =
    records.filter { it.state in MediaState.LISTABLE_STATES }

/**
 * Groups and maps a list of [MediaRecord] into the standard JSON representation for listing APIs.
 *
 * @param records The filtered list of records to expose.
 * @param timestampSelector A function extracting the timestamp (e.g. createdTime or deletionScheduledAt) for each record.
 * @return A JSON string mapping lowercase media types to lists of [MediaItem].
 */
internal fun formatMediaList(
    records: List<MediaRecord>,
    timestampSelector: (MediaRecord) -> Long
): String {
    val groups = records.groupBy { it.mediaType.lowercase() }
    val result = groups.mapValues { (_, list) ->
        list.map { record ->
            MediaItem(record.fileId, timestampSelector(record))
        }
    }
    return Json.encodeToString(result)
}
