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
 *
 * Track ordering note: CameraX Recorder guarantees that all tracks are added before
 * start() is called and before any samples are written, which satisfies Media3's
 * requirement that all tracks must be added before writing samples.
 */
@UnstableApi
class FragmentedMedia3Muxer : Muxer {

    private var muxer: FragmentedMp4Muxer? = null
    private var fileOutputStream: FileOutputStream? = null
    private var isStarted = false
    private var isReleased = false
    private val lock = Any()

    private var orientationDegrees: Int? = null
    private var location: Pair<Double, Double>? = null
    private var captureFps: Float? = null

    private fun checkNotStarted() {
        if (isStarted) throw IllegalStateException("Muxer already started")
    }

    private fun checkNotReleased() {
        if (isReleased) throw IllegalStateException("Muxer already released")
    }

    @SuppressLint("RestrictedApi")
    override fun setOutput(path: String, format: Int) {
        synchronized(lock) {
            checkNotStarted()
            checkNotReleased()
            require(format == Muxer.MUXER_FORMAT_MPEG_4) { "Only MPEG-4 format is supported" }
            if (fileOutputStream != null) throw IllegalStateException("Output already set")

            fileOutputStream = FileOutputStream(path)
            @Suppress("DEPRECATION")
            muxer = FragmentedMp4Muxer.Builder(fileOutputStream!!.channel).build()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun setOutput(parcelFileDescriptor: ParcelFileDescriptor, format: Int) {
        synchronized(lock) {
            checkNotStarted()
            checkNotReleased()
            require(format == Muxer.MUXER_FORMAT_MPEG_4) { "Only MPEG-4 format is supported" }
            if (fileOutputStream != null) throw IllegalStateException("Output already set")

            // AutoCloseOutputStream takes ownership and closes PFD when stream is closed.
            fileOutputStream = ParcelFileDescriptor.AutoCloseOutputStream(parcelFileDescriptor)
            @Suppress("DEPRECATION")
            muxer = FragmentedMp4Muxer.Builder(fileOutputStream!!.channel).build()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun setOrientationDegrees(degrees: Int) {
        synchronized(lock) {
            checkNotStarted()
            checkNotReleased()
            require(degrees == 0 || degrees == 90 || degrees == 180 || degrees == 270) {
                "Unsupported orientation degrees: $degrees"
            }
            orientationDegrees = degrees
        }
    }

    @SuppressLint("RestrictedApi")
    override fun setLocation(latitude: Double, longitude: Double) {
        synchronized(lock) {
            checkNotStarted()
            checkNotReleased()
            require(latitude in -90.0..90.0) { "Latitude must be in [-90, 90]" }
            require(longitude in -180.0..180.0) { "Longitude must be in [-180, 180]" }
            location = Pair(latitude, longitude)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun setCaptureFps(captureFps: Int) {
        synchronized(lock) {
            checkNotStarted()
            checkNotReleased()
            require(captureFps > 0) { "Capture FPS must be positive" }
            this.captureFps = captureFps.toFloat()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun isInterruptionResilient(): Boolean = true

    @SuppressLint("RestrictedApi")
    override fun addTrack(format: android.media.MediaFormat): Int {
        synchronized(lock) {
            checkNotStarted()
            checkNotReleased()
            val currentMuxer = muxer ?: throw IllegalStateException("Output not set")

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

            val colorStandard = if (format.containsKey(android.media.MediaFormat.KEY_COLOR_STANDARD)) format.getInteger(android.media.MediaFormat.KEY_COLOR_STANDARD) else Format.NO_VALUE
            val colorRange = if (format.containsKey(android.media.MediaFormat.KEY_COLOR_RANGE)) format.getInteger(android.media.MediaFormat.KEY_COLOR_RANGE) else Format.NO_VALUE
            val colorTransfer = if (format.containsKey(android.media.MediaFormat.KEY_COLOR_TRANSFER)) format.getInteger(android.media.MediaFormat.KEY_COLOR_TRANSFER) else Format.NO_VALUE
            val hdrStaticInfo = if (format.containsKey("hdr-static-info")) format.getByteBuffer("hdr-static-info")?.array() else null

            if (colorStandard != Format.NO_VALUE || colorRange != Format.NO_VALUE || colorTransfer != Format.NO_VALUE || hdrStaticInfo != null) {
                formatBuilder.setColorInfo(
                    ColorInfo.Builder()
                        .setColorSpace(colorStandard)
                        .setColorRange(colorRange)
                        .setColorTransfer(colorTransfer)
                        .setHdrStaticInfo(hdrStaticInfo)
                        .build()
                )
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
    }

    @SuppressLint("RestrictedApi")
    override fun writeSampleData(trackIndex: Int, byteBuffer: ByteBuffer, bufferInfo: AndroidBufferInfo) {
        synchronized(lock) {
            checkNotReleased()
            if (!isStarted) throw IllegalStateException("Muxer not started")
            val currentMuxer = muxer ?: throw IllegalStateException("Muxer not initialized")

            // Skip codec config samples because they are already provided as initialization data in addTrack()
            if ((bufferInfo.flags and android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                return
            }

            // Ensure limit/position logic doesn't overflow
            require(bufferInfo.offset >= 0 && bufferInfo.size >= 0 && bufferInfo.offset + bufferInfo.size <= byteBuffer.capacity()) {
                "Invalid bufferInfo offset or size"
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
                currentMuxer.writeSampleData(trackIndex, byteBuffer, media3BufferInfo)
            } finally {
                byteBuffer.position(originalPosition)
                byteBuffer.limit(originalLimit)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun start() {
        synchronized(lock) {
            checkNotStarted()
            checkNotReleased()
            val currentMuxer = muxer ?: throw IllegalStateException("Output not set")

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

            isStarted = true
        }
    }

    @SuppressLint("RestrictedApi")
    override fun stop() {
        synchronized(lock) {
            checkNotReleased()
            if (!isStarted) throw IllegalStateException("Muxer not started")

            // Just close the Media3 muxer on stop(). The release() method handles final PFD/Stream closure.
            muxer?.close()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun release() {
        synchronized(lock) {
            if (isReleased) return
            try {
                if (muxer != null) {
                    muxer?.close()
                }
            } catch (e: Exception) {
                Log.e("FragmentedMedia3Muxer", "Failed to close muxer on release", e)
            } finally {
                muxer = null
                try {
                    fileOutputStream?.close()
                } catch (e: Exception) {
                    Log.e("FragmentedMedia3Muxer", "Failed to close output stream", e)
                }
                fileOutputStream = null
                isReleased = true
                isStarted = false
            }
        }
    }
}
