/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.sdk.api.models

/**
 * Represents the response from a batch request to schedule media files for deletion.
 *
 * Vault permanently deletes scheduled files after its retention period, not immediately.
 *
 * @property successfulIds File IDs that were successfully scheduled for deletion (including files already scheduled).
 * @property failedIds File IDs whose scheduling was rejected, mapped to the error message.
 */
public data class BatchMediaDeleteResponse(
    val successfulIds: List<String>,
    val failedIds: Map<String, String>
)
