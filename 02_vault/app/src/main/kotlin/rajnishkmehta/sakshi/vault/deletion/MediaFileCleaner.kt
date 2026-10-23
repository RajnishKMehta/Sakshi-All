/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault.deletion

import rajnishkmehta.sakshi.sdk.api.validation.PathValidator
import rajnishkmehta.sakshi.vault.AppLog as Log
import rajnishkmehta.sakshi.vault.db.MediaRecord
import java.io.File
import java.io.IOException

/**
 * Permanently removes the Vault-private files that belong to a media record: the media file
 * itself and its thumbnail, if one exists.
 *
 * Only files inside the Vault's private `files/media` directory are touched. Public exported copies
 * and original Camera source files are never resolved or deleted by this class.
 *
 * Deletion is idempotent: files that are already missing count as removed, so an interrupted
 * cleanup can be retried safely.
 *
 * @param filesDir The application's private files directory (`Context.filesDir`).
 */
/**
 * Responsible for permanently removing the files associated with a [MediaRecord] from storage.
 *
 * Should only be called after all locking and eligibility checks have passed. Failures to delete
 * files throw an exception, signaling the cleanup coordinator to retry later.
 */
class MediaFileCleaner(private val filesDir: File) {

    private val tag = "MediaFileCleaner"

    /**
     * Deletes the media file and thumbnail of [record].
     *
     * @return `true` only if every Vault-private file of the record is gone afterwards.
     */
    fun deleteFiles(record: MediaRecord): Boolean {
        val targets = try {
            resolveTargets(record)
        } catch (e: IllegalArgumentException) {
            Log.e(tag, "Refusing to clean up ${record.fileId}: invalid stored path components", e)
            return false
        } catch (e: IOException) {
            Log.e(tag, "Refusing to clean up ${record.fileId}: cannot resolve storage paths", e)
            return false
        }

        var allRemoved = true
        for (target in targets) {
            if (!deleteIfPresent(record.fileId, target)) {
                allRemoved = false
            }
        }
        return allRemoved
    }

    private fun resolveTargets(record: MediaRecord): List<File> {
        PathValidator.validatePathComponents(record.fileId, record.mediaType, record.fileExtension)
        val mediaRoot = File(filesDir, MEDIA_DIR).canonicalFile
        val mediaTypeDir = File(mediaRoot, record.mediaType.lowercase())

        val targets = linkedSetOf(
            File(mediaTypeDir, "${record.fileId}.${record.fileExtension}").canonicalFile,
            File(mediaTypeDir, "$THUMBNAIL_DIR/${record.fileId}.$THUMBNAIL_EXTENSION").canonicalFile
        )
        record.vaultPath?.let { storedPath ->
            val storedFile = File(storedPath).canonicalFile
            if (storedFile.path.startsWith(mediaRoot.path + File.separator)) {
                targets += storedFile
            } else {
                Log.e(tag, "Ignoring stored vault path outside private media storage for ${record.fileId}")
            }
        }

        targets.forEach { target ->
            require(target.path.startsWith(mediaRoot.path + File.separator)) {
                "Resolved cleanup target escapes private media storage"
            }
        }
        return targets.toList()
    }

    private fun deleteIfPresent(fileId: String, file: File): Boolean = try {
        when {
            !file.exists() -> true
            file.isDirectory -> {
                Log.e(tag, "Cleanup target for $fileId is a directory; not deleting it")
                false
            }
            file.delete() || !file.exists() -> true
            else -> {
                Log.e(tag, "Failed to delete ${file.name} for $fileId")
                false
            }
        }
    } catch (e: SecurityException) {
        Log.e(tag, "Permission denied while deleting ${file.name} for $fileId", e)
        false
    }

    private companion object {
        const val MEDIA_DIR = "media"
        const val THUMBNAIL_DIR = "thumbnail"
        const val THUMBNAIL_EXTENSION = "webp"
    }
}
