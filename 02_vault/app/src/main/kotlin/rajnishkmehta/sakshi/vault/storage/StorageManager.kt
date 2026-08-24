package rajnishkmehta.sakshi.vault.storage

import java.io.InputStream

/**
 * Interface defining modular storage operations for the Sakshi Vault.
 * This abstracts how and where media files (photos, videos) are saved and appended,
 * making it easy to swap in encryption or cloud backends in the future.
 */
interface StorageManager {

    /**
     * Obtains the destination URI or path string where the given media file is stored.
     *
     * @param fileId Unique identifier of the file.
     * @param mediaType The type of media (e.g. "PHOTO", "VIDEO", "AUDIO", "UNKNOWN").
     * @param mimeType The precise MIME type of the media (e.g. "image/jpeg").
     * @return The URI/path string pointing to the destination.
     */
    fun getDestinationUri(fileId: String, mediaType: String, mimeType: String?): String

    /**
     * Stores a complete media file by reading from the provided [inputStream] and writing it to the destination.
     * Overwrites any existing destination file for the given [fileId].
     *
     * @param fileId Unique identifier of the media.
     * @param inputStream Source stream containing the media bytes.
     * @param mediaType The type of media (e.g. "PHOTO", "VIDEO", "AUDIO", "UNKNOWN").
     * @param mimeType The precise MIME type of the media (e.g. "image/jpeg").
     * @return The absolute path or content URI of the saved media.
     */
    fun saveMedia(fileId: String, inputStream: InputStream, mediaType: String, mimeType: String?): String

    /**
     * Appends newly written media bytes to the private destination file starting at the specified [offset].
     * This method seeks (or skips) in the [inputStream] to the [offset], and copies any new bytes to the end of the destination.
     *
     * @param fileId Unique identifier of the media.
     * @param inputStream Source stream containing the media bytes.
     * @param offset The starting position in the source stream where copying should resume.
     * @param mediaType The type of media (e.g. "PHOTO", "VIDEO", "AUDIO", "UNKNOWN").
     * @param mimeType The precise MIME type of the media (e.g. "video/mp4").
     * @return The number of newly copied bytes.
     */
    fun appendMediaBytes(fileId: String, inputStream: InputStream, offset: Long, mediaType: String, mimeType: String?): Long

    /**
     * Deletes the local media file associated with the given [fileId], [mediaType], and [mimeType].
     *
     * @param fileId Unique identifier of the media.
     * @param mediaType The type of media.
     * @param mimeType The precise MIME type of the media.
     * @return True if deletion was successful, false otherwise.
     */
    fun deleteMedia(fileId: String, mediaType: String, mimeType: String?): Boolean
}
