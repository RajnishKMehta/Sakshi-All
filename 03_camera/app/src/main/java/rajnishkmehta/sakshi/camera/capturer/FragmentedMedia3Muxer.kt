package rajnishkmehta.sakshi.camera.capturer

import android.annotation.SuppressLint

import android.media.MediaCodec
import android.os.ParcelFileDescriptor
import androidx.camera.video.internal.muxer.Muxer
import androidx.media3.muxer.FragmentedMp4Muxer
import java.io.FileOutputStream
import java.nio.ByteBuffer
import androidx.media3.common.util.UnstableApi

/**
 * A custom [Muxer] implementation utilizing Media3's [FragmentedMp4Muxer].
 *
 * This class handles writing captured video and audio streams into a Fragmented MP4 (fMP4) container.
 * It translates Android's [MediaFormat] to Media3's [androidx.media3.common.Format] to correctly
 * initialize tracks for muxing.
 */
@androidx.annotation.OptIn(UnstableApi::class)
class FragmentedMedia3Muxer : Muxer {

    private var muxer: FragmentedMp4Muxer? = null
    private val trackIds = mutableMapOf<Int, androidx.media3.muxer.Muxer.TrackToken>()
    private var outputSet = false

    @SuppressLint("RestrictedApi")
    override fun setOutput(path: String, format: Int) {
        val fos = FileOutputStream(path)
        muxer = FragmentedMp4Muxer.Builder(fos).build()
        outputSet = true
    }

    @SuppressLint("RestrictedApi")
    override fun setOutput(parcelFileDescriptor: ParcelFileDescriptor, format: Int) {
        val fos = FileOutputStream(parcelFileDescriptor.fileDescriptor)
        muxer = FragmentedMp4Muxer.Builder(fos).build()
        outputSet = true
    }

    @SuppressLint("RestrictedApi")
    override fun setOrientationDegrees(degrees: Int) {
        // Not currently exposed directly by FragmentedMp4Muxer.Builder without Metadata or Track options,
        // but typically handled at track level. Can be ignored or implemented if needed.
    }

    @SuppressLint("RestrictedApi")
    override fun setLocation(latitude: Double, longitude: Double) {
        // Ignored for fragmented MP4
    }

    @SuppressLint("RestrictedApi")
    override fun setCaptureFps(captureFps: Int) {
        // Ignored
    }

    @SuppressLint("RestrictedApi")
    override fun isInterruptionResilient(): Boolean {
        return true
    }

    @SuppressLint("RestrictedApi")
    override fun addTrack(format: android.media.MediaFormat): Int {
        val m = muxer ?: throw IllegalStateException("Muxer not initialized")

        // Media3 Muxer takes androidx.media3.common.Format.
        // Wait, FragmentedMp4Muxer has `addTrack(Format format)`. We need to convert MediaFormat to Format.
        // Or wait, does it? Media3 muxer's addTrack actually takes `Format`.
        // Let's check how CameraX Media3MuxerImpl does it.
        // For our test, we might need a workaround or check the exact API.

        // Wait, Media3 1.4.1 FragmentedMp4Muxer addTrack signature:
        // public TrackToken addTrack(Format format)
        // We will build a simple Media3 Format from the MediaFormat.

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

        // Get codec specific data (csd-0, csd-1)
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

        val token = m.addTrack(builder.build())
        val trackId = trackIds.size
        trackIds[trackId] = token
        return trackId
    }

    @SuppressLint("RestrictedApi")
    override fun writeSampleData(trackIndex: Int, byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        val m = muxer ?: return
        val token = trackIds[trackIndex] ?: return
        m.writeSampleData(token, byteBuffer, bufferInfo)
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
