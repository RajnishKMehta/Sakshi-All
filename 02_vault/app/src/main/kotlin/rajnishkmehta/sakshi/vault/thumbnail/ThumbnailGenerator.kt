package rajnishkmehta.sakshi.vault.thumbnail

import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.CancellationSignal
import android.util.Size
import java.io.File
import java.io.FileOutputStream
import rajnishkmehta.sakshi.sdk.api.validation.PathValidator
import rajnishkmehta.sakshi.vault.AppLog as Log

class ThumbnailGenerator(private val context: Context) {
    private val tag = "ThumbnailGenerator"

    fun generateThumbnail(fileId: String, mediaType: String, fileExtension: String, sourcePath: String) {
        if (mediaType != "PHOTO" && mediaType != "VIDEO") return

        try {
            PathValidator.validatePathComponents(fileId, mediaType, fileExtension)

            val sourceFile = File(sourcePath)
            if (!sourceFile.exists()) {
                Log.d(tag, "Source file does not exist for thumbnail generation: $sourcePath")
                return
            }

            val size = Size(256, 256)
            val signal = CancellationSignal()

            val bitmap = if (mediaType == "PHOTO") {
                ThumbnailUtils.createImageThumbnail(sourceFile, size, signal)
            } else {
                ThumbnailUtils.createVideoThumbnail(sourceFile, size, signal)
            }

            val mediaDir = File(context.filesDir, "media/${mediaType.lowercase()}/thumbnail")
            mediaDir.mkdirs()
            val thumbFile = File(mediaDir, "$fileId.$fileExtension")

            PathValidator.validateDestinationPath(context.filesDir, thumbFile)

            FileOutputStream(thumbFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            Log.d(tag, "Generated thumbnail for $fileId at ${thumbFile.absolutePath}")

        } catch (e: Exception) {
            Log.e(tag, "Failed to generate thumbnail for $fileId", e)
        }
    }
}
