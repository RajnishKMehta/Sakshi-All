/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.sdk.api.models

/**
 * Represents the response from a batch media deletion request.
 *
 * @property successfulIds A list of file IDs that were successfully deleted.
 * @property failedIds A map of file IDs that failed to delete, with the value being the error message.
 */
public data class BatchMediaDeleteResponse(
    val successfulIds: List<String>,
    val failedIds: Map<String, String>
)
