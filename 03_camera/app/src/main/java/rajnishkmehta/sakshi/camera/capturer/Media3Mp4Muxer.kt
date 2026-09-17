package rajnishkmehta.sakshi.camera.capturer

import android.annotation.SuppressLint

import android.media.MediaCodec.BufferInfo as AndroidBufferInfo
import android.os.ParcelFileDescriptor
import androidx.camera.video.internal.muxer.Muxer
import androidx.media3.muxer.Mp4Muxer
import androidx.media3.muxer.BufferInfo
import java.io.FileOutputStream
import java.nio.ByteBuffer
import androidx.media3.common.util.UnstableApi
import androidx.annotation.OptIn

/**
 * A custom [Muxer] implementation utilizing Media3's [Mp4Muxer].
 *
 * This class handles writing captured video and audio streams into a standard MP4 container.
 * It translates Android's [android.media.MediaFormat] to Media3's [androidx.media3.common.Format] to correctly
 * initialize tracks for muxing, fully replacing the built-in MediaMuxerImpl.
 */
@OptIn(UnstableApi::class)
class Media3Mp4Muxer : Muxer {

    private var muxer: Mp4Muxer? = null
    private var outputSet = false

    @SuppressLint("RestrictedApi")
    override fun setOutput(path: String, format: Int) {
        val fos = FileOutputStream(path)
        muxer = Mp4Muxer.Builder(fos).build()
        outputSet = true
    }

    @SuppressLint("RestrictedApi")
    override fun setOutput(parcelFileDescriptor: ParcelFileDescriptor, format: Int) {
        val fos = FileOutputStream(parcelFileDescriptor.fileDescriptor)
        muxer = Mp4Muxer.Builder(fos).build()
        outputSet = true
    }

    @SuppressLint("RestrictedApi")
    override fun setOrientationDegrees(degrees: Int) {
        // Not exposed directly by Mp4Muxer without specific track updates. Ignored for now.
    }

    @SuppressLint("RestrictedApi")
    override fun setLocation(latitude: Double, longitude: Double) {
        // Not currently mapped
    }

    @SuppressLint("RestrictedApi")
    override fun setCaptureFps(captureFps: Int) {
        // Ignored
    }

    @SuppressLint("RestrictedApi")
    override fun isInterruptionResilient(): Boolean {
        return false // MP4 (non-fragmented) is typically not interruption resilient
    }

    @SuppressLint("RestrictedApi")
    override fun addTrack(format: android.media.MediaFormat): Int {
        val m = muxer ?: throw IllegalStateException("Muxer not initialized")

        val mimeType = format.getString(android.media.MediaFormat.KEY_MIME)
        val builder = androidx.media3.common.Format.Builder().setSampleMimeType(mimeType)

        if (format.containsKey(android.media.MediaFormat.KEY_WIDTH)) {
            builder.setWidth(format.getInteger(android.media.MediaFormat.KEY_WIDTH))
        }
        if (format.containsKey(android.media.MediaFormat.KEY_HEIGHT)) {
            builder.setHeight(format.getInteger(android.media.MediaFormat.KEY_HEIGHT))
        }
        if (format.containsKey(android.media.MediaFormat.KEY_CHANNEL_COUNT)) {
            builder.setChannelCount(format.getInteger(android.media.MediaFormat.KEY_CHANNEL_COUNT))
        }
        if (format.containsKey(android.media.MediaFormat.KEY_SAMPLE_RATE)) {
            builder.setSampleRate(format.getInteger(android.media.MediaFormat.KEY_SAMPLE_RATE))
        }

        val initializationData = mutableListOf<ByteArray>()
        var csdIndex = 0
        while (true) {
            val csdKey = "csd-$csdIndex"
            if (format.containsKey(csdKey)) {
                val buffer = format.getByteBuffer(csdKey)
                if (buffer != null) {
                    val bytes = ByteArray(buffer.capacity())
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
        builder.setInitializationData(initializationData)

        return m.addTrack(builder.build())
    }

    @SuppressLint("RestrictedApi")
    override fun writeSampleData(trackIndex: Int, byteBuffer: ByteBuffer, bufferInfo: AndroidBufferInfo) {
        val m = muxer ?: return

        val media3BufferInfo = BufferInfo(
            bufferInfo.presentationTimeUs,
            bufferInfo.size,
            bufferInfo.flags
        )

        val oldPosition = byteBuffer.position()
        val oldLimit = byteBuffer.limit()

        try {
            byteBuffer.position(bufferInfo.offset)
            byteBuffer.limit(bufferInfo.offset + bufferInfo.size)
        } catch (e: Exception) {
            throw androidx.camera.video.internal.muxer.MuxerException("Failed to write sample data", e)
        } finally {
            byteBuffer.limit(oldLimit)
            byteBuffer.position(oldPosition)
        }

    }

    @SuppressLint("RestrictedApi")
    override fun start() {
    }

    @SuppressLint("RestrictedApi")
    override fun stop() {
    }

    @SuppressLint("RestrictedApi")
    override fun release() {
        muxer?.close()
        muxer = null
    }
}
