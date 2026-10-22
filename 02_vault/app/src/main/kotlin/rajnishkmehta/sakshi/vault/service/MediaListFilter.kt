/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.service

import rajnishkmehta.sakshi.vault.db.MediaRecord
import rajnishkmehta.sakshi.vault.db.MediaState

/**
 * Returns the records exposed through `listMedia`: only [MediaState.LISTABLE_STATES].
 * Records that are still synchronizing or scheduled for deletion ([MediaState.DELETE]) are excluded.
 */
internal fun filterListableRecords(records: List<MediaRecord>): List<MediaRecord> =
    records.filter { it.state in MediaState.LISTABLE_STATES }
