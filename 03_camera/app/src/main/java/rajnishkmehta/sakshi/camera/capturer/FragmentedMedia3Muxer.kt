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

import androidx.media3.container.MdtaMetadataEntry
import androidx.media3.container.Mp4LocationData
import androidx.media3.container.Mp4OrientationData

/**
 * A custom [Muxer] implementation utilizing Media3's [FragmentedMp4Muxer].
 *
 * This class translates Android's native media components to Media3 constructs
 * and manages the lifecycle of Fragmented MP4 (fMP4) muxing. It securely handles
 * file descriptors and enforces strict presentation timestamp rules required by Media3.
 */
@UnstableApi
class FragmentedMedia3Muxer : Muxer {

    private var muxer: FragmentedMp4Muxer? = null
    private var fileOutputStream: FileOutputStream? = null
    private val lastPresentationTimesUs = mutableMapOf<Int, Long>()
    private var storedPfd: ParcelFileDescriptor? = null

    private var orientationDegrees: Int? = null
    private var location: Pair<Double, Double>? = null
    private var captureFps: Float? = null
    private var metadataAdded = false

    /**
     * Initializes the muxer output to the specified file path.
     *
     * @param path The absolute path to the output file.
     * @param format The container format requested by the caller.
     */
    @SuppressLint("RestrictedApi")
    override fun setOutput(path: String, format: Int) {
        val fos = FileOutputStream(path)
        fileOutputStream = fos
        @Suppress("DEPRECATION")
        muxer = FragmentedMp4Muxer.Builder(fos.channel).build()
    }

    /**
     * Initializes the muxer output using a parcel file descriptor.
     * Keeps a strong reference to the descriptor to prevent premature garbage collection.
     *
     * @param parcelFileDescriptor The file descriptor for the output.
     * @param format The container format requested by the caller.
     */
    @SuppressLint("RestrictedApi")
    override fun setOutput(parcelFileDescriptor: ParcelFileDescriptor, format: Int) {
        storedPfd = parcelFileDescriptor
        val fos = FileOutputStream(parcelFileDescriptor.fileDescriptor)
        fileOutputStream = fos
        @Suppress("DEPRECATION")
        muxer = FragmentedMp4Muxer.Builder(fos.channel).build()
    }

    /**
     * Sets the orientation hint for the video track.
     *
     * @param degrees The orientation angle in degrees.
     */
    @SuppressLint("RestrictedApi")
    override fun setOrientationDegrees(degrees: Int) { orientationDegrees = degrees }

    /**
     * Sets the geographic location metadata.
     *
     * @param latitude The latitude coordinate.
     * @param longitude The longitude coordinate.
     */
    @SuppressLint("RestrictedApi")
    override fun setLocation(latitude: Double, longitude: Double) { location = Pair(latitude, longitude) }

    /**
     * Sets the capture frames per second.
     *
     * @param captureFps The frame rate.
     */
    @SuppressLint("RestrictedApi")
    override fun setCaptureFps(captureFps: Int) { this.captureFps = captureFps.toFloat() }

    /**
     * Indicates whether this muxer is resilient to interruptions,
     * which is true for fragmented MP4 formats.
     *
     * @return `true` since fMP4 can be partially recovered on interruption.
     */
    @SuppressLint("RestrictedApi")
    override fun isInterruptionResilient(): Boolean = true

    /**
     * Translates an Android [android.media.MediaFormat] to a Media3 [Format]
     * and adds the track to the muxer.
     *
     * @param format The format of the track being added.
     * @return The integer index of the newly added track.
     */
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

        if (mimeType != null && mimeType.startsWith("audio/")) {
            if (format.containsKey(android.media.MediaFormat.KEY_LANGUAGE)) {
                formatBuilder.setLanguage(format.getString(android.media.MediaFormat.KEY_LANGUAGE))
            }
            if (format.containsKey(android.media.MediaFormat.KEY_MAX_INPUT_SIZE)) {
                formatBuilder.setMaxInputSize(format.getInteger(android.media.MediaFormat.KEY_MAX_INPUT_SIZE))
            }

        }


        val initializationData = mutableListOf<ByteArray>()
        var csdIndex = 0
        while (true) {
            val csdKey = "csd-$csdIndex"
            if (format.containsKey(csdKey)) {
                val buffer = format.getByteBuffer(csdKey)
                if (buffer != null) {
                    val bytes = ByteArray(buffer.remaining())
                    val originalPos = buffer.position()
                    buffer.get(bytes)
                    buffer.position(originalPos)
                    initializationData.add(bytes)
                }
                csdIndex++
            } else {
                break
            }
        }

        if (mimeType == android.media.MediaFormat.MIMETYPE_AUDIO_AAC) {
            var profile = -1
            if (format.containsKey(android.media.MediaFormat.KEY_PROFILE)) {
                profile = format.getInteger(android.media.MediaFormat.KEY_PROFILE)
            } else if (format.containsKey(android.media.MediaFormat.KEY_AAC_PROFILE)) {
                profile = format.getInteger(android.media.MediaFormat.KEY_AAC_PROFILE)
            }

            if (profile != -1 && format.containsKey(android.media.MediaFormat.KEY_SAMPLE_RATE) && format.containsKey(android.media.MediaFormat.KEY_CHANNEL_COUNT)) {
                val sampleRate = format.getInteger(android.media.MediaFormat.KEY_SAMPLE_RATE)
                val channelCount = format.getInteger(android.media.MediaFormat.KEY_CHANNEL_COUNT)

                val sampleRateIndex = when (sampleRate) {
                    96000 -> 0
                    88200 -> 1
                    64000 -> 2
                    48000 -> 3
                    44100 -> 4
                    32000 -> 5
                    24000 -> 6
                    22050 -> 7
                    16000 -> 8
                    12000 -> 9
                    11025 -> 10
                    8000 -> 11
                    7350 -> 12
                    else -> 4
                }

                val config = ByteArray(2)
                config[0] = ((profile shl 3) or (sampleRateIndex shr 1)).toByte()
                config[1] = (((sampleRateIndex and 0x01) shl 7) or (channelCount shl 3)).toByte()

                initializationData.clear()
                initializationData.add(config)
            }
        }

        formatBuilder.setInitializationData(initializationData)

        val isVideo = mimeType != null && mimeType.startsWith("video/")
        if (!metadataAdded && isVideo) {
            orientationDegrees?.let { degrees ->
                currentMuxer.addMetadataEntry(Mp4OrientationData(degrees))
            }
            location?.let { loc ->
                currentMuxer.addMetadataEntry(Mp4LocationData(loc.first.toFloat(), loc.second.toFloat()))
            }
            captureFps?.let { fps ->
                val fpsBytes = ByteBuffer.allocate(4).putInt(java.lang.Float.floatToIntBits(fps)).array()
                currentMuxer.addMetadataEntry(MdtaMetadataEntry(MdtaMetadataEntry.KEY_ANDROID_CAPTURE_FPS, fpsBytes, MdtaMetadataEntry.TYPE_INDICATOR_FLOAT32))
            }
            metadataAdded = true
        }

        return currentMuxer.addTrack(formatBuilder.build())
    }

    /**
     * Writes sample data to the specified track.
     * Adjusts byte buffer boundaries and enforces monotonic presentation timestamps
     * to satisfy Media3 muxer requirements.
     *
     * @param trackIndex The index of the destination track.
     * @param byteBuffer The buffer containing the encoded sample.
     * @param bufferInfo The metadata associated with the sample.
     */
    @SuppressLint("RestrictedApi")
    override fun writeSampleData(trackIndex: Int, byteBuffer: ByteBuffer, bufferInfo: AndroidBufferInfo) {
        val currentMuxer = muxer ?: return

        val originalPosition = byteBuffer.position()
        val originalLimit = byteBuffer.limit()

        try {
            byteBuffer.position(bufferInfo.offset)
            byteBuffer.limit(bufferInfo.offset + bufferInfo.size)

            var presentationTimeUs = bufferInfo.presentationTimeUs
            val lastTimeUs = lastPresentationTimesUs[trackIndex] ?: -1L
            if (presentationTimeUs <= lastTimeUs) {
                presentationTimeUs = lastTimeUs + 1L
            }
            lastPresentationTimesUs[trackIndex] = presentationTimeUs

            val media3BufferInfo = Media3BufferInfo(
                presentationTimeUs,
                bufferInfo.size,
                bufferInfo.flags
            )
            currentMuxer.writeSampleData(trackIndex, byteBuffer, media3BufferInfo)
        } finally {
            byteBuffer.position(originalPosition)
            byteBuffer.limit(originalLimit)
        }
    }

    /**
     * Signals the muxer to start writing.
     * Media3 muxers process data directly on `writeSampleData`, so this is a no-op.
     */
    @SuppressLint("RestrictedApi")
    override fun start() { }

    /**
     * Signals the muxer to stop.
     * Closing the muxer handles the finalization, so this is a no-op.
     */
    @SuppressLint("RestrictedApi")
    override fun stop() { }

    /**
     * Closes the muxer and releases associated output streams and file descriptors.
     */
    @SuppressLint("RestrictedApi")
    override fun release() {
        try {
            muxer?.close()
        } finally {
            muxer = null
            try {
                fileOutputStream?.close()
            } catch (e: Exception) {
                Log.e("FragmentedMedia3Muxer", "Failed to close output stream", e)
            }
            fileOutputStream = null
            storedPfd = null
        }
    }
}
