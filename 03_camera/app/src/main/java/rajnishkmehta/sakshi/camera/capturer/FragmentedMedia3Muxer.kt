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
 */
@UnstableApi
class FragmentedMedia3Muxer : Muxer {

    private var muxer: FragmentedMp4Muxer? = null
    private var fileOutputStream: FileOutputStream? = null
    private val lastPresentationTimesUs = mutableMapOf<Int, Long>()

    @SuppressLint("RestrictedApi")
    override fun setOutput(path: String, format: Int) {
        Log.d("FragmentedMedia3Muxer", "setOutput path: $path, format: $format")
        val fos = FileOutputStream(path)
        fileOutputStream = fos
        @Suppress("DEPRECATION")
        muxer = FragmentedMp4Muxer.Builder(fos.channel).build()
    }

    private var storedPfd: ParcelFileDescriptor? = null

    @SuppressLint("RestrictedApi")
    override fun setOutput(parcelFileDescriptor: ParcelFileDescriptor, format: Int) {
        Log.d("FragmentedMedia3Muxer", "setOutput FD, format: $format")
        storedPfd = parcelFileDescriptor // Prevent Garbage Collection from closing the FD prematurely
        val fos = ParcelFileDescriptor.AutoCloseOutputStream(parcelFileDescriptor)
        fileOutputStream = fos
        @Suppress("DEPRECATION")
        muxer = FragmentedMp4Muxer.Builder(fos.channel).build()
    }

    @SuppressLint("RestrictedApi")
    override fun setOrientationDegrees(degrees: Int) {
        Log.d("FragmentedMedia3Muxer", "setOrientationDegrees degrees: $degrees")
    }

    @SuppressLint("RestrictedApi")
    override fun setLocation(latitude: Double, longitude: Double) { }

    @SuppressLint("RestrictedApi")
    override fun setCaptureFps(captureFps: Int) { }

    @SuppressLint("RestrictedApi")
    override fun isInterruptionResilient(): Boolean = true

    @SuppressLint("RestrictedApi")
    override fun addTrack(format: android.media.MediaFormat): Int {
        Log.d("FragmentedMedia3Muxer", "addTrack format: $format")
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

        val originalPosition = byteBuffer.position()
        val originalLimit = byteBuffer.limit()

        try {
            // Log.d("FragmentedMedia3Muxer", "writeSampleData trackIndex: $trackIndex, size: ${bufferInfo.size}, time: ${bufferInfo.presentationTimeUs}, offset: ${bufferInfo.offset}, originalPosition: $originalPosition")

            // Media3 expects position and limit to strictly bound the sample data
            byteBuffer.position(bufferInfo.offset)
            byteBuffer.limit(bufferInfo.offset + bufferInfo.size)

            // Ensure strictly monotonic presentation timestamps
            var presentationTimeUs = bufferInfo.presentationTimeUs
            val lastTimeUs = lastPresentationTimesUs[trackIndex] ?: -1L
            if (presentationTimeUs <= lastTimeUs) {
                Log.w("FragmentedMedia3Muxer", "Adjusting non-monotonic timestamp for track $trackIndex. Last: $lastTimeUs, Current: $presentationTimeUs")
                presentationTimeUs = lastTimeUs + 1L
            }
            lastPresentationTimesUs[trackIndex] = presentationTimeUs

            val media3BufferInfo = Media3BufferInfo(
                presentationTimeUs,
                bufferInfo.size,
                bufferInfo.flags
            )
            currentMuxer.writeSampleData(trackIndex, byteBuffer, media3BufferInfo)
        } catch (e: Exception) {
            Log.e("FragmentedMedia3Muxer", "Exception during writeSampleData for track $trackIndex", e)
            throw e // Let it crash so it can be noticed but logged properly
        } finally {
            byteBuffer.position(originalPosition)
            byteBuffer.limit(originalLimit)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun start() {
        Log.d("FragmentedMedia3Muxer", "start")
    }

    @SuppressLint("RestrictedApi")
    override fun stop() {
        Log.d("FragmentedMedia3Muxer", "stop")
    }

    @SuppressLint("RestrictedApi")
    override fun release() {
        Log.d("FragmentedMedia3Muxer", "release")
        try {
            muxer?.close()
        } catch (e: Exception) {
            Log.e("FragmentedMedia3Muxer", "Exception during muxer close", e)
        } finally {
            muxer = null
            try {
                fileOutputStream?.close()
            } catch (e: Exception) {
                Log.e("FragmentedMedia3Muxer", "Exception closing output stream", e)
            }
            fileOutputStream = null
            storedPfd = null
        }
    }
}
