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

    @SuppressLint("RestrictedApi")
    override fun setOutput(path: String, format: Int) {
        synchronized(lock) {
            check(state == State.UNINITIALIZED) { "Muxer already initialized" }
            require(format == Muxer.MUXER_FORMAT_MPEG_4) { "Only MPEG-4 format is supported." }

            val fos = FileOutputStream(path)
            fileOutputStream = fos
            try {
                @Suppress("DEPRECATION")
                muxer = FragmentedMp4Muxer.Builder(fos.channel).build()
                state = State.INITIALIZED
            } catch (e: Exception) {
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

    @SuppressLint("RestrictedApi")
    override fun setOutput(parcelFileDescriptor: ParcelFileDescriptor, format: Int) {
        synchronized(lock) {
            check(state == State.UNINITIALIZED) { "Muxer already initialized" }
            require(format == Muxer.MUXER_FORMAT_MPEG_4) { "Only MPEG-4 format is supported." }

            val stream = ParcelFileDescriptor.AutoCloseOutputStream(parcelFileDescriptor)
            autoCloseStream = stream
            try {
                @Suppress("DEPRECATION")
                muxer = FragmentedMp4Muxer.Builder(stream.channel).build()
                state = State.INITIALIZED
            } catch (e: Exception) {
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

    @SuppressLint("RestrictedApi")
    override fun setOrientationDegrees(degrees: Int) {
        synchronized(lock) {
            check(state == State.INITIALIZED) { "Cannot set orientation after muxer is started or before initialization" }
            require(degrees == 0 || degrees == 90 || degrees == 180 || degrees == 270) {
                "Invalid orientation degrees: $degrees"
            }
            orientationDegrees = degrees
        }
    }

    @SuppressLint("RestrictedApi")
    override fun setLocation(latitude: Double, longitude: Double) {
        synchronized(lock) {
            check(state == State.INITIALIZED) { "Cannot set location after muxer is started" }
            require(latitude in -90.0..90.0) { "Latitude must be in range [-90, 90]" }
            require(longitude in -180.0..180.0) { "Longitude must be in range [-180, 180]" }
            location = Pair(latitude, longitude)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun setCaptureFps(captureFps: Int) {
        synchronized(lock) {
            check(state == State.INITIALIZED) { "Cannot set capture FPS after muxer is started" }
            require(captureFps > 0) { "Capture FPS must be greater than 0" }
            this.captureFps = captureFps.toFloat()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun isInterruptionResilient(): Boolean = true

    @SuppressLint("RestrictedApi")
    override fun addTrack(format: android.media.MediaFormat): Int {
        synchronized(lock) {
            check(state == State.INITIALIZED) { "Cannot add tracks after muxer has started or before initialization" }
            val currentMuxer = muxer ?: throw IllegalStateException("FragmentedMp4Muxer is not initialized")

            val mimeType = format.getString(android.media.MediaFormat.KEY_MIME)
            require(!mimeType.isNullOrBlank()) { "MIME type is missing or blank" }
            require(mimeType.startsWith("video/") || mimeType.startsWith("audio/")) { "Unsupported MIME type: $mimeType" }

            val formatBuilder = Format.Builder().setSampleMimeType(mimeType)

            if (mimeType.startsWith("video/")) {
                require(format.containsKey(android.media.MediaFormat.KEY_WIDTH) && format.containsKey(android.media.MediaFormat.KEY_HEIGHT)) {
                    "Video format requires KEY_WIDTH and KEY_HEIGHT"
                }
            } else if (mimeType.startsWith("audio/")) {
                require(format.containsKey(android.media.MediaFormat.KEY_SAMPLE_RATE) && format.containsKey(android.media.MediaFormat.KEY_CHANNEL_COUNT)) {
                    "Audio format requires KEY_SAMPLE_RATE and KEY_CHANNEL_COUNT"
                }
            }

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
                when (val std = format.getInteger(android.media.MediaFormat.KEY_COLOR_STANDARD)) {
                    android.media.MediaFormat.COLOR_STANDARD_BT709 -> androidx.media3.common.C.COLOR_SPACE_BT709
                    android.media.MediaFormat.COLOR_STANDARD_BT601_PAL,
                    android.media.MediaFormat.COLOR_STANDARD_BT601_NTSC -> androidx.media3.common.C.COLOR_SPACE_BT601
                    android.media.MediaFormat.COLOR_STANDARD_BT2020 -> androidx.media3.common.C.COLOR_SPACE_BT2020
                    else -> throw IllegalArgumentException("Unsupported color standard: $std")
                }
            } else androidx.media3.common.Format.NO_VALUE

            val media3ColorTransfer = if (format.containsKey(android.media.MediaFormat.KEY_COLOR_TRANSFER)) {
                when (val transfer = format.getInteger(android.media.MediaFormat.KEY_COLOR_TRANSFER)) {
                    android.media.MediaFormat.COLOR_TRANSFER_LINEAR -> androidx.media3.common.C.COLOR_TRANSFER_LINEAR
                    android.media.MediaFormat.COLOR_TRANSFER_SDR_VIDEO -> androidx.media3.common.C.COLOR_TRANSFER_SDR
                    android.media.MediaFormat.COLOR_TRANSFER_ST2084 -> androidx.media3.common.C.COLOR_TRANSFER_ST2084
                    android.media.MediaFormat.COLOR_TRANSFER_HLG -> androidx.media3.common.C.COLOR_TRANSFER_HLG
                    else -> throw IllegalArgumentException("Unsupported color transfer: $transfer")
                }
            } else androidx.media3.common.Format.NO_VALUE

            val media3ColorRange = if (format.containsKey(android.media.MediaFormat.KEY_COLOR_RANGE)) {
                when (val range = format.getInteger(android.media.MediaFormat.KEY_COLOR_RANGE)) {
                    android.media.MediaFormat.COLOR_RANGE_LIMITED -> androidx.media3.common.C.COLOR_RANGE_LIMITED
                    android.media.MediaFormat.COLOR_RANGE_FULL -> androidx.media3.common.C.COLOR_RANGE_FULL
                    else -> throw IllegalArgumentException("Unsupported color range: $range")
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

            if (mimeType == android.media.MediaFormat.MIMETYPE_VIDEO_AVC) {
                require(format.containsKey("csd-0")) { "Missing csd-0 for AVC" }
                require(format.containsKey("csd-1")) { "Missing csd-1 for AVC" }
            } else if (mimeType == android.media.MediaFormat.MIMETYPE_VIDEO_HEVC || mimeType == android.media.MediaFormat.MIMETYPE_AUDIO_AAC) {
                require(format.containsKey("csd-0")) { "Missing csd-0 for $mimeType" }
            }

            val initializationData = mutableListOf<ByteArray>()
            var csdIndex = 0
            while (true) {
                val csdKey = "csd-$csdIndex"
                if (format.containsKey(csdKey)) {
                    val buffer = format.getByteBuffer(csdKey)
                    if (buffer != null) {
                        require(buffer.capacity() > 0) { "CSD buffer $csdKey is empty" }
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

            return currentMuxer.addTrack(formatBuilder.build())
        }
    }

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
            currentMuxer.writeSampleData(trackIndex, duplicateBuffer, media3BufferInfo)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun stop() {
        synchronized(lock) {
            check(state == State.STARTED) { "Muxer is not started (state: $state)" }
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

    @SuppressLint("RestrictedApi")
    override fun release() {
        synchronized(lock) {
            if (state == State.RELEASED) return

            if (state != State.STOPPED && state != State.UNINITIALIZED) {
                Log.w("FragmentedMedia3Muxer", "Releasing muxer from invalid state: $state. Should have called stop() first.")
            }

            var primaryException: Exception? = null
            try {
                if (state == State.STARTED) {
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
