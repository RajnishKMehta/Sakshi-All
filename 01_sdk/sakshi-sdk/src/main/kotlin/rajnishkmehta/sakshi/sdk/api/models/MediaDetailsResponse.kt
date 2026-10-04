/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.sdk.api.models

/**
 * Represents details of a media file retrieved from Vault.
 *
 * @property contentUri The real ContentResolver URI to access the media file.
 * @property fileId The unique identifier of the media file.
 * @property mediaType The type of media (e.g., "PHOTO", "VIDEO").
 * @property fileExtension The extension of the media file.
 * @property createdTime The creation timestamp of the media file.
 */
public data class MediaDetailsResponse(
    val contentUri: String,
    val fileId: String,
    val mediaType: String,
    val fileExtension: String,
    val createdTime: Long
)
