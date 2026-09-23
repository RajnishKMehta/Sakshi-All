package rajnishkmehta.sakshi.vault.storage

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.RandomAccessFile
import rajnishkmehta.sakshi.sdk.api.validation.PathValidator

/**
 * Concrete implementation of [StorageManager] that stores media in the application's private files directory.
 * This storage is secure because it cannot be accessed by other applications on the device.
 */
class AppPrivateStorageManager(private val context: Context) : StorageManager {

    override fun getDestinationUri(fileId: String, mediaType: String, fileExtension: String): String {
        PathValidator.validatePathComponents(fileId, mediaType, fileExtension)

        val mediaDir = File(context.filesDir, "media/${mediaType.lowercase()}")
        mediaDir.mkdirs()
        val fileName = "${fileId}.$fileExtension"
        val file = File(mediaDir, fileName)

        PathValidator.validateDestinationPath(context.filesDir, file)

        return file.absolutePath
    }

    override fun saveMedia(fileId: String, inputStream: InputStream, mediaType: String, fileExtension: String): String {
        PathValidator.validatePathComponents(fileId, mediaType, fileExtension)
        val destinationPath = getDestinationUri(fileId, mediaType, fileExtension)
        val destinationFile = File(destinationPath)

        // Ensure any existing file is deleted first to overwrite completely
        if (destinationFile.exists()) {
            destinationFile.delete()
        }

        FileOutputStream(destinationFile).use { output ->
            inputStream.copyTo(output)
        }
        return destinationFile.absolutePath
    }

    /**
     * Synchronizes the leading media metadata and appends bytes after [offset].
     *
     * For an existing file, up to 128 KiB is rewritten from the start of [inputStream] so
     * mutable headers, such as MP4 metadata, stay current during incremental copies.
     *
     * @return the number of bytes appended after [offset], excluding synchronized header bytes.
     */
    override fun appendMediaBytes(fileId: String, inputStream: InputStream, offset: Long, mediaType: String, fileExtension: String): Long {
        PathValidator.validatePathComponents(fileId, mediaType, fileExtension)
        val destinationPath = getDestinationUri(fileId, mediaType, fileExtension)
        val destinationFile = File(destinationPath)

        // Ensure parent directories exist
        destinationFile.parentFile?.mkdirs()

        // Resiliency Mitigation: Truncate destination file to the expected DB offset
        // to prevent duplicate bytes if a previous sync crashed mid-pass
        if (destinationFile.exists() && destinationFile.length() > offset) {
            FileOutputStream(destinationFile, true).use { fos ->
                val channel = fos.channel
                channel.truncate(offset)
            }
        }

        // The first few KB (typically up to 128KB) contain critical video headers (like moov or mdat size updates for mp4).
        // If the file exists and we are appending, we need to read this header from the input stream and overwrite the existing file's header.
        var skipOffset = offset
        if (offset > 0 && destinationFile.exists()) {
            val headerSize = minOf(offset, 128 * 1024L) // Synchronize up to 128KB of header
            val headerBuffer = ByteArray(headerSize.toInt())
            var headerBytesRead = 0
            while (headerBytesRead < headerSize) {
                val read = inputStream.read(headerBuffer, headerBytesRead, (headerSize - headerBytesRead).toInt())
                if (read == -1) break
                headerBytesRead += read
            }
            if (headerBytesRead > 0) {
                RandomAccessFile(destinationFile, "rw").use { raf ->
                    raf.seek(0)
                    raf.write(headerBuffer, 0, headerBytesRead)
                }
            }
            skipOffset = offset - headerBytesRead
        }

        // Seek (skip) to the requested offset in the source input stream
        inputStream.skipFully(skipOffset)

        var bytesCopied = 0L
        FileOutputStream(destinationFile, true).use { output ->
            val buffer = ByteArray(8192)
            var bytesRead = inputStream.read(buffer)
            while (bytesRead != -1) {
                output.write(buffer, 0, bytesRead)
                bytesCopied += bytesRead
                bytesRead = inputStream.read(buffer)
            }
        }
        return bytesCopied
    }

    override fun deleteMedia(fileId: String, mediaType: String, fileExtension: String): Boolean {
        PathValidator.validatePathComponents(fileId, mediaType, fileExtension)
        val path = getDestinationUri(fileId, mediaType, fileExtension)
        val file = File(path)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    /**
     * Extension helper to skip exactly [n] bytes from the stream, handling partial skips robustly.
     */
    private fun InputStream.skipFully(n: Long) {
        var remaining = n
        while (remaining > 0) {
            val skipped = skip(remaining)
            if (skipped <= 0) {
                // If skip returns 0, try to read 1 byte to check for EOF or force progress
                if (read() == -1) {
                    break // EOF reached
                }
                remaining--
            } else {
                remaining -= skipped
            }
        }
    }
}
