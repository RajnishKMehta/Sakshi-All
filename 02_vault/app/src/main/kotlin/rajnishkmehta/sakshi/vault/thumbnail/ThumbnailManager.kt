package rajnishkmehta.sakshi.vault.thumbnail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Size
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Presentation
import androidx.media3.inspector.frame.FrameExtractor
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rajnishkmehta.sakshi.vault.AppLog as Log

/**
 * Responsible for generating and storing thumbnails for fully copied media files.
 */
object ThumbnailManager {
    private const val TAG = "ThumbnailManager"
    // Sensible small thumbnail size for Portal/gallery preview
    private const val MAX_DIMENSION = 320

    /**
     * Generates a thumbnail for a given source media file and saves it in the private Vault storage.
     * Only processes PHOTO and VIDEO types.
     *
     * @param context Application context
     * @param fileId Unique file identifier
     * @param mediaType The media type ("PHOTO", "VIDEO", etc.)
     * @param sourceFile The fully copied source file in vault storage
     * @return The generated thumbnail File, or null if generation was skipped or failed.
     */
    @OptIn(UnstableApi::class)
    suspend fun generateAndStoreThumbnail(
        context: Context,
        fileId: String,
        mediaType: String,
        sourceFile: File
    ): File? = withContext(Dispatchers.IO) {
        if (mediaType != "PHOTO" && mediaType != "VIDEO") {
            Log.d(TAG, "Skipping thumbnail generation for unsupported media type: $mediaType")
            return@withContext null
        }

        if (!sourceFile.exists() || sourceFile.length() == 0L) {
            Log.e(TAG, "Cannot generate thumbnail: Source file does not exist or is empty ($fileId)")
            return@withContext null
        }

        try {
            val thumbnailDir = File(context.filesDir, "media/${mediaType.lowercase()}/thumbnail")
            thumbnailDir.mkdirs()

            val thumbnailFile = File(thumbnailDir, "${fileId}.webp")
            Log.d(TAG, "Generating thumbnail for $fileId ($mediaType) at ${thumbnailFile.absolutePath}")

            val bitmap: Bitmap = if (mediaType == "PHOTO") {
                val source = ImageDecoder.createSource(sourceFile)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE

                    val width = info.size.width
                    val height = info.size.height
                    val maxOriginal = maxOf(1, maxOf(width, height))

                    if (maxOriginal > MAX_DIMENSION) {
                        val scale = MAX_DIMENSION.toFloat() / maxOriginal
                        val targetWidth = maxOf(1, (width * scale).toInt())
                        val targetHeight = maxOf(1, (height * scale).toInt())
                        decoder.setTargetSize(targetWidth, targetHeight)
                    }
                }
            } else {
                var frameExtractor: FrameExtractor? = null
                try {
                    val mediaItem = MediaItem.fromUri(Uri.fromFile(sourceFile))
                    frameExtractor = FrameExtractor.Builder(context, mediaItem)
                        .setEffects(listOf(Presentation.createForShortSide(MAX_DIMENSION)))
                        .build()
                    val frame = frameExtractor.thumbnail.get()
                    frame.bitmap
                } finally {
                    frameExtractor?.close()
                }
            }

            @Suppress("DEPRECATION")
            val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                Bitmap.CompressFormat.WEBP
            }

            var compressSuccess = false
            for (attempt in 1..2) {
                FileOutputStream(thumbnailFile).use { out ->
                    compressSuccess = bitmap.compress(format, 80, out)
                }
                if (compressSuccess) {
                    break
                }
                thumbnailFile.delete()
            }

            if (!compressSuccess) {
                Log.e(TAG, "Failed to compress thumbnail for $fileId")
                return@withContext null
            }

            Log.d(TAG, "Successfully generated thumbnail for $fileId")
            return@withContext thumbnailFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate thumbnail for $fileId", e)
            return@withContext null
        }
    }
}
