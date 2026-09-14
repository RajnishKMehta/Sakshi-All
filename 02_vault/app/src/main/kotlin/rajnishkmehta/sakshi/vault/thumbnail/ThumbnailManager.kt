package rajnishkmehta.sakshi.vault.thumbnail

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.os.Build
import android.util.Size
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
    private val THUMBNAIL_SIZE = Size(320, 320)
    private const val MAX_THUMBNAIL_DIMENSION = 320

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

            val thumbnailFile = File(thumbnailDir, "$fileId.webp")
            Log.d(TAG, "Generating thumbnail for $fileId ($mediaType) at ${thumbnailFile.absolutePath}")

            val bitmap: Bitmap? = if (mediaType == "PHOTO") {
                ThumbnailUtils.createImageThumbnail(sourceFile, THUMBNAIL_SIZE, null)
            } else {
                var extractedBitmap: Bitmap? = null
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(sourceFile.absolutePath)
                    val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                    val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                    var width = widthStr?.toIntOrNull() ?: 0
                    var height = heightStr?.toIntOrNull() ?: 0

                    val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
                    if (rotation == 90 || rotation == 270) {
                        val tmp = width
                        width = height
                        height = tmp
                    }

                    if (width <= 0 || height <= 0) {
                        Log.e(TAG, "Aborting video thumbnail: Invalid dimensions ${width}x$height for $fileId")
                        return@withContext null
                    }

                    var targetWidth = width
                    var targetHeight = height
                    if (width > MAX_THUMBNAIL_DIMENSION || height > MAX_THUMBNAIL_DIMENSION) {
                        val scale = MAX_THUMBNAIL_DIMENSION.toFloat() / maxOf(width, height)
                        targetWidth = maxOf(1, (width * scale).toInt())
                        targetHeight = maxOf(1, (height * scale).toInt())
                    }

                    extractedBitmap = retriever.getScaledFrameAtTime(-1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, targetWidth, targetHeight)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed video thumbnail extraction for $fileId", e)
                } finally {
                    retriever.release()
                }
                extractedBitmap
            }

            if (bitmap == null) {
                Log.e(TAG, "Failed to create bitmap for $fileId")
                return@withContext null
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
