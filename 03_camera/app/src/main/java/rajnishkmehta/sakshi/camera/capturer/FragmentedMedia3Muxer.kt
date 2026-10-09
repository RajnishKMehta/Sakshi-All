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
     * Indicates whether this muxer is resilient to interruptions.
     * Fragmented MP4 (fMP4) writes data in fragments, making it more resilient than standard MP4.
     * If the process terminates abruptly, fragments that have already been completely
     * written and flushed will remain playable. Un-flushed trailing samples may be lost.
     *
     * @return `true` since fMP4 allows partial recovery of the file on interruption.
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

            val media3ColorSpace = if (format.containsKey(android.media.MediaFormat.KEY_COLOR_STANDARD)) {
                when (format.getInteger(android.media.MediaFormat.KEY_COLOR_STANDARD)) {
                    android.media.MediaFormat.COLOR_STANDARD_BT709 -> androidx.media3.common.C.COLOR_SPACE_BT709
                    android.media.MediaFormat.COLOR_STANDARD_BT601_PAL,
                    android.media.MediaFormat.COLOR_STANDARD_BT601_NTSC -> androidx.media3.common.C.COLOR_SPACE_BT601
                    android.media.MediaFormat.COLOR_STANDARD_BT2020 -> androidx.media3.common.C.COLOR_SPACE_BT2020
                    else -> androidx.media3.common.Format.NO_VALUE
                }
            } else androidx.media3.common.Format.NO_VALUE

            val media3ColorTransfer = if (format.containsKey(android.media.MediaFormat.KEY_COLOR_TRANSFER)) {
                when (format.getInteger(android.media.MediaFormat.KEY_COLOR_TRANSFER)) {
                    android.media.MediaFormat.COLOR_TRANSFER_LINEAR -> androidx.media3.common.C.COLOR_TRANSFER_LINEAR
                    android.media.MediaFormat.COLOR_TRANSFER_SDR_VIDEO -> androidx.media3.common.C.COLOR_TRANSFER_SDR
                    android.media.MediaFormat.COLOR_TRANSFER_ST2084 -> androidx.media3.common.C.COLOR_TRANSFER_ST2084
                    android.media.MediaFormat.COLOR_TRANSFER_HLG -> androidx.media3.common.C.COLOR_TRANSFER_HLG
                    else -> androidx.media3.common.Format.NO_VALUE
                }
            } else androidx.media3.common.Format.NO_VALUE

            val media3ColorRange = if (format.containsKey(android.media.MediaFormat.KEY_COLOR_RANGE)) {
                when (format.getInteger(android.media.MediaFormat.KEY_COLOR_RANGE)) {
                    android.media.MediaFormat.COLOR_RANGE_LIMITED -> androidx.media3.common.C.COLOR_RANGE_LIMITED
                    android.media.MediaFormat.COLOR_RANGE_FULL -> androidx.media3.common.C.COLOR_RANGE_FULL
                    else -> androidx.media3.common.Format.NO_VALUE
                }
            } else androidx.media3.common.Format.NO_VALUE

            val hdrStaticInfo = if (format.containsKey("hdr-static-info")) {
                val buffer = format.getByteBuffer("hdr-static-info")
                if (buffer != null) {
                    val duplicate = buffer.duplicate()
                    val bytes = ByteArray(duplicate.remaining())
                    duplicate.get(bytes)
                    bytes
                } else null
            } else null

            if (media3ColorSpace != androidx.media3.common.Format.NO_VALUE ||
                media3ColorTransfer != androidx.media3.common.Format.NO_VALUE ||
                media3ColorRange != androidx.media3.common.Format.NO_VALUE ||
                hdrStaticInfo != null) {

                val colorInfo = ColorInfo.Builder()
                    .setColorSpace(media3ColorSpace)
                    .setColorTransfer(media3ColorTransfer)
                    .setColorRange(media3ColorRange)
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
            if (mimeType != null) {
                val needsCsd = mimeType == android.media.MediaFormat.MIMETYPE_VIDEO_AVC ||
                               mimeType == android.media.MediaFormat.MIMETYPE_VIDEO_HEVC ||
                               mimeType == android.media.MediaFormat.MIMETYPE_AUDIO_AAC
                if (needsCsd && initializationData.isEmpty()) {
                    throw IllegalArgumentException("Missing required codec-specific data (csd-0) for mimeType: $mimeType")
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

            try {
                orientationDegrees?.let { degrees ->
                    currentMuxer.addMetadataEntry(Mp4OrientationData(degrees))
                }
                location?.let { loc ->
                    currentMuxer.addMetadataEntry(Mp4LocationData(loc.first.toFloat(), loc.second.toFloat()))
                }
                captureFps?.let { fps ->
                    val fpsBytes = ByteBuffer.allocate(4).putFloat(fps).array()
                    currentMuxer.addMetadataEntry(MdtaMetadataEntry(MdtaMetadataEntry.KEY_ANDROID_CAPTURE_FPS, fpsBytes, MdtaMetadataEntry.TYPE_INDICATOR_FLOAT32))
                }

                state = State.STARTED
            } catch (e: Exception) {
                Log.e("FragmentedMedia3Muxer", "Failed to start muxer", e)
                throw e
            }
        }
    }

    /**
     * Writes sample data to the specified track.
     * Adjusts byte buffer boundaries and passes through presentation timestamps directly.
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

            val offset = bufferInfo.offset
            val size = bufferInfo.size
            val capacity = byteBuffer.capacity()
            require(offset >= 0 && size >= 0) { "Negative offset or size: offset=$offset, size=$size" }
            require(offset <= capacity) { "Offset out of bounds: offset=$offset, capacity=$capacity" }
            require(size <= capacity - offset) { "Size out of bounds: size=$size, maxAllowed=${capacity - offset}" }
            require(bufferInfo.presentationTimeUs >= 0) { "Invalid presentation time: ${bufferInfo.presentationTimeUs}" }

            // Skip codec config samples because they are already provided as initialization data in addTrack()
            if ((bufferInfo.flags and android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                return
            }

            val duplicateBuffer = byteBuffer.duplicate()
            duplicateBuffer.clear()
            duplicateBuffer.position(bufferInfo.offset)
            duplicateBuffer.limit(bufferInfo.offset + bufferInfo.size)

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
            currentMuxer.writeSampleData(trackIndex, duplicateBuffer, media3BufferInfo)
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
                var exception: Exception? = null
                try {
                    muxer?.close()
                } catch (e: Exception) {
                    Log.e("FragmentedMedia3Muxer", "Failed to close muxer on stop", e)
                    exception = e
                } finally {
                    state = State.STOPPED
                    muxer = null
                }

                if (exception != null) {
                    throw exception
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

            var primaryException: Exception? = null
            try {
                if (state == State.STARTED || state == State.INITIALIZED) {
                    muxer?.close()
                }
            } catch (e: Exception) {
                Log.e("FragmentedMedia3Muxer", "Failed to close muxer on release", e)
                primaryException = e
            } finally {
                muxer = null
                try {
                    fileOutputStream?.close()
                } catch (e: Exception) {
                    Log.e("FragmentedMedia3Muxer", "Failed to close FileOutputStream", e)
                    if (primaryException != null) primaryException.addSuppressed(e) else primaryException = e
                }
                try {
                    autoCloseStream?.close()
                } catch (e: Exception) {
                    Log.e("FragmentedMedia3Muxer", "Failed to close AutoCloseOutputStream", e)
                    if (primaryException != null) primaryException.addSuppressed(e) else primaryException = e
                }
                fileOutputStream = null
                autoCloseStream = null
                state = State.RELEASED

                if (primaryException != null) {
                    throw primaryException
                }
            }
        }
    }
}
