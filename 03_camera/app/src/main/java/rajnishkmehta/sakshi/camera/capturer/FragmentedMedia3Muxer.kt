/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
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
import androidx.media3.common.ColorInfo
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

    private enum class State {
        UNINITIALIZED, INITIALIZED, STARTED, STOPPED, RELEASED
    }

    private val lock = Any()
    private var state = State.UNINITIALIZED
    private var muxer: FragmentedMp4Muxer? = null
    private var fileOutputStream: FileOutputStream? = null
    private var autoCloseStream: ParcelFileDescriptor.AutoCloseOutputStream? = null

    private var orientationDegrees: Int? = null
    private var location: Pair<Double, Double>? = null
    private var captureFps: Float? = null


    /**
     * Initializes the muxer output to the specified file path.
     *
     * @param path The absolute path to the output file.
     * @param format The container format requested by the caller.
     */
    @SuppressLint("RestrictedApi")
    override fun setOutput(path: String, format: Int) {
        synchronized(lock) {
            check(state == State.UNINITIALIZED) { "Muxer already initialized" }
            require(format == Muxer.MUXER_FORMAT_MPEG_4) { "Only MPEG-4 format is supported. Expected ${Muxer.MUXER_FORMAT_MPEG_4} (MPEG_4), got $format" }

            val fos = FileOutputStream(path)
            fileOutputStream = fos
            try {
                @Suppress("DEPRECATION")
                muxer = FragmentedMp4Muxer.Builder(fos.channel).build()
                state = State.INITIALIZED
            } catch (e: Exception) {
                // Do not leak stream on failure
                try {
                    fos.close()
                } catch (ce: Exception) {
                    e.addSuppressed(ce)
                }
                fileOutputStream = null
                throw e
            }
        }
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
        synchronized(lock) {
            check(state == State.UNINITIALIZED) { "Muxer already initialized" }
            require(format == Muxer.MUXER_FORMAT_MPEG_4) { "Only MPEG-4 format is supported. Expected ${Muxer.MUXER_FORMAT_MPEG_4} (MPEG_4), got $format" }

            val stream = ParcelFileDescriptor.AutoCloseOutputStream(parcelFileDescriptor)
            autoCloseStream = stream
            try {
                @Suppress("DEPRECATION")
                muxer = FragmentedMp4Muxer.Builder(stream.channel).build()
                state = State.INITIALIZED
            } catch (e: Exception) {
                // Do not leak stream on failure
                try {
                    stream.close()
                } catch (ce: Exception) {
                    e.addSuppressed(ce)
                }
                autoCloseStream = null
                throw e
            }
        }
    }

    /**
     * Sets the orientation hint for the video track.
     *
     * @param degrees The orientation angle in degrees.
     */
    @SuppressLint("RestrictedApi")
    override fun setOrientationDegrees(degrees: Int) {
        synchronized(lock) {
            check(state == State.INITIALIZED) { "Cannot set orientation after muxer is started" }
            require(degrees == 0 || degrees == 90 || degrees == 180 || degrees == 270) {
                "Invalid orientation degrees: $degrees"
            }
            orientationDegrees = degrees
        }
    }

    /**
     * Sets the geographic location metadata.
     *
     * @param latitude The latitude coordinate.
     * @param longitude The longitude coordinate.
     */
    @SuppressLint("RestrictedApi")
    override fun setLocation(latitude: Double, longitude: Double) {
        synchronized(lock) {
            check(state == State.INITIALIZED) { "Cannot set location after muxer is started" }
            require(latitude in -90.0..90.0) { "Latitude must be in range [-90, 90]" }
            require(longitude in -180.0..180.0) { "Longitude must be in range [-180, 180]" }
            location = Pair(latitude, longitude)
        }
    }

    /**
     * Sets the capture frames per second.
     *
     * @param captureFps The frame rate.
     */
    @SuppressLint("RestrictedApi")
    override fun setCaptureFps(captureFps: Int) {
        synchronized(lock) {
            check(state == State.INITIALIZED) { "Cannot set capture FPS after muxer is started" }
            require(captureFps > 0) { "Capture FPS must be greater than 0" }
            this.captureFps = captureFps.toFloat()
        }
    }

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
        synchronized(lock) {
            check(state == State.INITIALIZED) { "Cannot add tracks after muxer has started or before initialization" }
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

            if (format.containsKey(android.media.MediaFormat.KEY_COLOR_STANDARD)) {
                val colorStandard = format.getInteger(android.media.MediaFormat.KEY_COLOR_STANDARD)
                val colorTransfer = if (format.containsKey(android.media.MediaFormat.KEY_COLOR_TRANSFER)) format.getInteger(android.media.MediaFormat.KEY_COLOR_TRANSFER) else androidx.media3.common.Format.NO_VALUE
                val colorRange = if (format.containsKey(android.media.MediaFormat.KEY_COLOR_RANGE)) format.getInteger(android.media.MediaFormat.KEY_COLOR_RANGE) else androidx.media3.common.Format.NO_VALUE

                val hdrStaticInfo = if (format.containsKey("hdr-static-info")) {
                    val buffer = format.getByteBuffer("hdr-static-info")
                    if (buffer != null) {
                        val duplicate = buffer.duplicate()
                        val bytes = ByteArray(duplicate.remaining())
                        duplicate.get(bytes)
                        bytes
                    } else null
                } else null

                val colorInfo = ColorInfo.Builder()
                    .setColorSpace(colorStandard)
                    .setColorTransfer(colorTransfer)
                    .setColorRange(colorRange)
                    .setHdrStaticInfo(hdrStaticInfo)
                    .build()
                formatBuilder.setColorInfo(colorInfo)
            }

            val initializationData = mutableListOf<ByteArray>()
            var csdIndex = 0
            while (true) {
                val csdKey = "csd-$csdIndex"
                if (format.containsKey(csdKey)) {
                    val buffer = format.getByteBuffer(csdKey)
                    if (buffer != null) {
                        val duplicate = buffer.duplicate()
                        val bytes = ByteArray(duplicate.remaining())
                        duplicate.get(bytes)
                        initializationData.add(bytes)
                    }
                    csdIndex++
                } else {
                    break
                }
            }
            formatBuilder.setInitializationData(initializationData)

            val trackIndex = currentMuxer.addTrack(formatBuilder.build())
            return trackIndex
        }
    }

    /**
     * Signals the muxer to start writing.
     * Media3 muxers process data directly on `writeSampleData`, so this is a no-op.
     */
    @SuppressLint("RestrictedApi")
    override fun start() {
        synchronized(lock) {
            check(state == State.INITIALIZED) { "Cannot start muxer from state $state" }
            val currentMuxer = muxer ?: throw IllegalStateException("FragmentedMp4Muxer is not initialized")

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

            state = State.STARTED
        }
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
        synchronized(lock) {
            check(state == State.STARTED) { "Muxer is not started (state: $state)" }
            val currentMuxer = muxer ?: throw IllegalStateException("FragmentedMp4Muxer is not initialized")

            require(bufferInfo.offset >= 0 && bufferInfo.size >= 0 && bufferInfo.offset <= byteBuffer.capacity() && bufferInfo.size <= byteBuffer.capacity() - bufferInfo.offset) {
                "Invalid buffer info: offset=${bufferInfo.offset}, size=${bufferInfo.size}, capacity=${byteBuffer.capacity()}"
            }

            // Skip codec config samples because they are already provided as initialization data in addTrack()
            if ((bufferInfo.flags and android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                return
            }

            val originalPosition = byteBuffer.position()
            val originalLimit = byteBuffer.limit()

            try {
                byteBuffer.position(bufferInfo.offset)
                byteBuffer.limit(bufferInfo.offset + bufferInfo.size)

                var media3Flags = 0
                if ((bufferInfo.flags and android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0) {
                    media3Flags = media3Flags or androidx.media3.common.C.BUFFER_FLAG_KEY_FRAME
                }
                if ((bufferInfo.flags and android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    media3Flags = media3Flags or androidx.media3.common.C.BUFFER_FLAG_END_OF_STREAM
                }

                val media3BufferInfo = Media3BufferInfo(
                    bufferInfo.presentationTimeUs,
                    bufferInfo.size,
                    media3Flags
                )
                // Media3's writeSampleData copies the data so we do not need to clone the buffer here
                currentMuxer.writeSampleData(trackIndex, byteBuffer, media3BufferInfo)
            } finally {
                byteBuffer.position(originalPosition)
                byteBuffer.limit(originalLimit)
            }
        }
    }

    /**
     * Signals the muxer to stop.
     * Closing the muxer handles the finalization.
     */
    @SuppressLint("RestrictedApi")
    override fun stop() {
        synchronized(lock) {
            if (state == State.STARTED) {
                try {
                    muxer?.close()
                } catch (e: Exception) {
                    Log.e("FragmentedMedia3Muxer", "Failed to close muxer on stop", e)
                    throw e
                } finally {
                    state = State.STOPPED
                    muxer = null
                }
            }
        }
    }

    /**
     * Closes the muxer and releases associated output streams and file descriptors.
     */
    @SuppressLint("RestrictedApi")
    override fun release() {
        synchronized(lock) {
            if (state == State.RELEASED) return

            if (state != State.STOPPED && state != State.UNINITIALIZED) {
                Log.w("FragmentedMedia3Muxer", "Releasing muxer from invalid state: $state. Should have called stop() first.")
            }
            try {
                if (state == State.STARTED || state == State.INITIALIZED) {
                    muxer?.close()
                }
            } catch (e: Exception) {
                Log.e("FragmentedMedia3Muxer", "Failed to close muxer on release", e)
            } finally {
                muxer = null
                try {
                    fileOutputStream?.close()
                } catch (e: Exception) {
                    Log.e("FragmentedMedia3Muxer", "Failed to close FileOutputStream", e)
                }
                try {
                    autoCloseStream?.close()
                } catch (e: Exception) {
                    Log.e("FragmentedMedia3Muxer", "Failed to close AutoCloseOutputStream", e)
                }
                fileOutputStream = null
                autoCloseStream = null
                state = State.RELEASED
            }
        }
    }
}
