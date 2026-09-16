package rajnishkmehta.sakshi.camera.capturer

import android.annotation.SuppressLint
import android.media.MediaCodec
import android.os.ParcelFileDescriptor
import androidx.camera.video.internal.muxer.Muxer
import androidx.media3.muxer.FragmentedMp4Muxer
import java.io.FileOutputStream
import java.nio.ByteBuffer
import androidx.media3.common.util.UnstableApi
import androidx.annotation.OptIn
import java.lang.reflect.Method
import android.util.Log

/**
 * A custom [Muxer] implementation utilizing Media3's [FragmentedMp4Muxer].
 *
 * This class handles writing captured video and audio streams into a Fragmented MP4 (fMP4) container.
 * It translates Android's [MediaFormat] to Media3's [androidx.media3.common.Format] to correctly
 * initialize tracks for muxing.
 */
@OptIn(UnstableApi::class)
class FragmentedMedia3Muxer : Muxer {

    private var muxer: FragmentedMp4Muxer? = null
    private val trackIds = mutableMapOf<Int, androidx.media3.muxer.Muxer.TrackToken>()
    private var outputSet = false
    private var isStarted = false

    private var writeSampleDataMethod: Method? = null
    private var bufferInfoConstructor: java.lang.reflect.Constructor<*>? = null

    init {
        try {
            val bufferInfoClass = Class.forName("androidx.media3.muxer.BufferInfo")
            bufferInfoConstructor = bufferInfoClass.getConstructor(Long::class.java, Int::class.java, Int::class.java, Int::class.java)
            writeSampleDataMethod = FragmentedMp4Muxer::class.java.getMethod("writeSampleData", androidx.media3.muxer.Muxer.TrackToken::class.java, ByteBuffer::class.java, bufferInfoClass)
        } catch (e: Exception) {
            Log.e("FragmentedMedia3Muxer", "Failed to resolve Media3 muxer methods", e)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun setOutput(path: String, format: Int) {
        val fos = FileOutputStream(path)
        muxer = FragmentedMp4Muxer.Builder(fos)
            .setFragmentDurationMs(1000) // Fragment every 1 second
            .build()
        outputSet = true
    }

    @SuppressLint("RestrictedApi")
    override fun setOutput(parcelFileDescriptor: ParcelFileDescriptor, format: Int) {
        val fos = FileOutputStream(parcelFileDescriptor.fileDescriptor)
        muxer = FragmentedMp4Muxer.Builder(fos)
            .setFragmentDurationMs(1000) // Fragment every 1 second
            .build()
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

        if (isStarted) {
             throw IllegalStateException("Cannot add track after start")
        }

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

        val token = m.addTrack(builder.build())
        val trackId = trackIds.size
        trackIds[trackId] = token
        return trackId
    }

    @SuppressLint("RestrictedApi")
    override fun writeSampleData(trackIndex: Int, byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        val m = muxer ?: return
        val token = trackIds[trackIndex] ?: return

        try {
            if (writeSampleDataMethod != null && bufferInfoConstructor != null) {
                val media3BufferInfo = bufferInfoConstructor!!.newInstance(
                    bufferInfo.presentationTimeUs,
                    bufferInfo.size,
                    bufferInfo.offset,
                    bufferInfo.flags
                )
                writeSampleDataMethod!!.invoke(m, token, byteBuffer, media3BufferInfo)
            } else {
                // Fallback to older Media3 version if method is not found (e.g. 1.4.1 might use MediaCodec.BufferInfo directly via a different overload or extension)
                // Actually 1.9.0 requires androidx.media3.muxer.BufferInfo, so we have to use reflection to bypass Kotlin compilation errors.
            }
        } catch (e: Exception) {
            Log.e("FragmentedMedia3Muxer", "Failed to write sample data", e)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun start() {
        isStarted = true
    }

    @SuppressLint("RestrictedApi")
    override fun stop() {
    }

    @SuppressLint("RestrictedApi")
    override fun release() {
        try {
            muxer?.close()
        } catch (e: Exception) {
            Log.e("FragmentedMedia3Muxer", "Error closing muxer", e)
        }
        muxer = null
    }
}
