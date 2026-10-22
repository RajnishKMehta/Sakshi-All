/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.sdk.api.models

/**
 * Represents the response from a batch request to recover media files previously scheduled for deletion.
 *
 * Vault restores the file to its valid state, preventing its permanent deletion.
 *
 * @property successfulIds File IDs that were successfully recovered (including files that were never scheduled for deletion).
 * @property failedIds File IDs whose recovery was rejected (e.g., invalid ID, already permanently deleted), mapped to the error message.
 */
public data class BatchMediaRecoverResponse(
    val successfulIds: List<String>,
    val failedIds: Map<String, String>
)
