/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.deletion

/**
 * Outcome of scheduling a single media file for deletion.
 */
sealed interface DeletionScheduleResult {

    /** Identifier of the media file the result refers to. */
    val fileId: String

    /**
     * The file is in [rajnishkmehta.sakshi.vault.db.MediaState.DELETE] and will be permanently
     * deleted once the retention period elapsed.
     *
     * @property scheduledAt Wall-clock time (epoch milliseconds) at which deletion was originally scheduled.
     * @property alreadyScheduled `true` if the file was already scheduled by an earlier request. Repeated
     * requests are idempotent and never extend the retention period.
     */
    data class Scheduled(
        override val fileId: String,
        val scheduledAt: Long,
        val alreadyScheduled: Boolean
    ) : DeletionScheduleResult

    /**
     * The request was rejected and the record was left unchanged.
     *
     * @property reason Human-readable reason that is safe to return over IPC.
     */
    data class Rejected(
        override val fileId: String,
        val reason: String
    ) : DeletionScheduleResult
}

/**
 * Outcome of scheduling multiple media files for deletion, matching the SDK's
 * `BatchMediaDeleteResponse` contract.
 *
 * @property successfulIds Identifiers that are scheduled for deletion, in request order.
 * @property failedIds Identifiers that were rejected, mapped to the rejection reason.
 */
data class BatchDeletionScheduleResult(
    val successfulIds: List<String>,
    val failedIds: Map<String, String>
)
