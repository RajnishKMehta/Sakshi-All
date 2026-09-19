package rajnishkmehta.sakshi.camera.capturer

import android.annotation.SuppressLint
import rajnishkmehta.sakshi.camera.debug.DebugLogger as Log
import android.media.MediaCodec.BufferInfo as AndroidBufferInfo
import android.os.ParcelFileDescriptor
import androidx.camera.video.internal.muxer.Muxer
import androidx.media3.muxer.FragmentedMp4Muxer
import androidx.media3.muxer.BufferInfo as Media3BufferInfo
import java.io.FileOutputStream
import java.nio.ByteBuffer
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.Format

/**
 * A custom [Muxer] implementation utilizing Media3's [FragmentedMp4Muxer].
 *
 * This class handles writing captured video and audio streams into a Fragmented MP4 (fMP4) container.
 * It follows the official CameraX architecture for translating Android's [android.media.MediaFormat]
 * to Media3's [Format] in order to correctly initialize tracks for fMP4 muxing.
 */
@UnstableApi
class FragmentedMedia3Muxer : Muxer {

    private var muxer: FragmentedMp4Muxer? = null
    private var fileOutputStream: FileOutputStream? = null

    @SuppressLint("RestrictedApi")
    override fun setOutput(path: String, format: Int) {
        val fos = FileOutputStream(path)
        fileOutputStream = fos
        muxer = FragmentedMp4Muxer.Builder(fos.channel).build()
    }

    @SuppressLint("RestrictedApi")
    override fun setOutput(parcelFileDescriptor: ParcelFileDescriptor, format: Int) {
        val fos = FileOutputStream(parcelFileDescriptor.fileDescriptor)
        fileOutputStream = fos
        muxer = FragmentedMp4Muxer.Builder(fos.channel).build()
    }

    @SuppressLint("RestrictedApi")
    override fun setOrientationDegrees(degrees: Int) {
        // Not currently exposed by FragmentedMp4Muxer.Builder directly.
    }

    @SuppressLint("RestrictedApi")
    override fun setLocation(latitude: Double, longitude: Double) {
        // Ignored for fragmented MP4.
    }

    @SuppressLint("RestrictedApi")
    override fun setCaptureFps(captureFps: Int) {
        // Ignored.
    }

    @SuppressLint("RestrictedApi")
    override fun isInterruptionResilient(): Boolean {
        return true
    }

    @SuppressLint("RestrictedApi")
    override fun addTrack(format: android.media.MediaFormat): Int {
        val currentMuxer = muxer ?: throw IllegalStateException("FragmentedMp4Muxer is not initialized")

        val mimeType = format.getString(android.media.MediaFormat.KEY_MIME)
        val formatBuilder = Format.Builder().setSampleMimeType(mimeType)

        if (format.containsKey(android.media.MediaFormat.KEY_WIDTH)) {
            formatBuilder.setWidth(format.getInteger(android.media.MediaFormat.KEY_WIDTH))
        }
        if (format.containsKey(android.media.MediaFormat.KEY_HEIGHT)) {
            formatBuilder.setHeight(format.getInteger(android.media.MediaFormat.KEY_HEIGHT))
        }
        if (format.containsKey(android.media.MediaFormat.KEY_CHANNEL_COUNT)) {
            formatBuilder.setChannelCount(format.getInteger(android.media.MediaFormat.KEY_CHANNEL_COUNT))
        }
        if (format.containsKey(android.media.MediaFormat.KEY_SAMPLE_RATE)) {
            formatBuilder.setSampleRate(format.getInteger(android.media.MediaFormat.KEY_SAMPLE_RATE))
        }
        if (format.containsKey(android.media.MediaFormat.KEY_BIT_RATE)) {
            formatBuilder.setAverageBitrate(format.getInteger(android.media.MediaFormat.KEY_BIT_RATE))
        }

        val initializationData = mutableListOf<ByteArray>()
        var csdIndex = 0
        while (true) {
            val csdKey = "csd-$csdIndex"
            if (format.containsKey(csdKey)) {
                val buffer = format.getByteBuffer(csdKey)
                if (buffer != null) {
                    val bytes = ByteArray(buffer.remaining())
                    buffer.position(0)
                    buffer.get(bytes)
                    buffer.position(0)
                    initializationData.add(bytes)
                }
                csdIndex++
            } else {
                break
            }
        }
        formatBuilder.setInitializationData(initializationData)

        return currentMuxer.addTrack(formatBuilder.build())
    }

    @SuppressLint("RestrictedApi")
    override fun writeSampleData(trackIndex: Int, byteBuffer: ByteBuffer, bufferInfo: AndroidBufferInfo) {
        val currentMuxer = muxer ?: return
        val media3BufferInfo = Media3BufferInfo(
            bufferInfo.presentationTimeUs,
            bufferInfo.size,
            bufferInfo.flags
        )
        currentMuxer.writeSampleData(trackIndex, byteBuffer, media3BufferInfo)
    }

    @SuppressLint("RestrictedApi")
    override fun start() {
        // No-op for Media3 Muxer API (it writes as samples are added).
    }

    @SuppressLint("RestrictedApi")
    override fun stop() {
        // The Muxer doesn't have an explicit stop, closing it finalizes.
    }

    @SuppressLint("RestrictedApi")
    override fun release() {
        muxer?.close()
        muxer = null
        try {
            fileOutputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        fileOutputStream = null
    }
}
