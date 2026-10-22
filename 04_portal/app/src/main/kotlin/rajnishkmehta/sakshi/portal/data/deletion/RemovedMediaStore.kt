/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.portal.data.deletion

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory store of media IDs that Vault confirmed as scheduled for deletion during this session.
 *
 * The gallery hides these IDs immediately, without waiting for the next `listMedia` refresh
 * (which also excludes them because Vault no longer lists scheduled files).
 */
class RemovedMediaStore {

    private val _removedIds = MutableStateFlow<Set<String>>(emptySet())

    /** IDs of media removed from the gallery. */
    val removedIds: StateFlow<Set<String>> = _removedIds.asStateFlow()

    /** Hides [fileId] from the gallery. */
    fun markRemoved(fileId: String) {
        _removedIds.update { it + fileId }
    }
}
