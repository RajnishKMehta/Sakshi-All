package rajnishkmehta.sakshi.vault.storage

import android.content.Context
import rajnishkmehta.sakshi.vault.utils.MimeTypeHelper
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Concrete implementation of [StorageManager] that stores media in the application's private files directory.
 * This storage is secure because it cannot be accessed by other applications on the device.
 */
class AppPrivateStorageManager(private val context: Context) : StorageManager {

    override fun getDestinationUri(fileId: String, mediaType: String, mimeType: String?): String {
        val extension = MimeTypeHelper.getExtensionFromMimeType(mimeType)
        val fileName = if (extension != null) "vault_${fileId}.$extension" else "vault_${fileId}"
        val file = File(context.filesDir, fileName)
        return file.absolutePath
    }

    override fun saveMedia(fileId: String, inputStream: InputStream, mediaType: String, mimeType: String?): String {
        val destinationPath = getDestinationUri(fileId, mediaType, mimeType)
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

    override fun appendMediaBytes(fileId: String, inputStream: InputStream, offset: Long, mediaType: String, mimeType: String?): Long {
        val destinationPath = getDestinationUri(fileId, mediaType, mimeType)
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

        // Seek (skip) to the requested offset in the source input stream
        inputStream.skipFully(offset)

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

    override fun deleteMedia(fileId: String, mediaType: String, mimeType: String?): Boolean {
        val path = getDestinationUri(fileId, mediaType, mimeType)
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
