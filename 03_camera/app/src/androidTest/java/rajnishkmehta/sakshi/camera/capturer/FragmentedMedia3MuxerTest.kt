package rajnishkmehta.sakshi.camera.capturer

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.ParcelFileDescriptor
import androidx.camera.video.internal.muxer.Muxer
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.File
import java.nio.ByteBuffer

@RunWith(AndroidJUnit4::class)
class FragmentedMedia3MuxerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var classUnderTest: FragmentedMedia3Muxer
    private lateinit var outputFile: File

    @Before
    fun setUp() {
        classUnderTest = FragmentedMedia3Muxer()
        outputFile = tempFolder.newFile("test.mp4")
    }

    @Test
    fun testInitialization_successfulPath() {
        classUnderTest.setOutput(outputFile.absolutePath, Muxer.MUXER_FORMAT_MPEG_4)
        // Should not throw
    }

    @Test
    fun testInitialization_successfulFd() {
        val pfd = ParcelFileDescriptor.open(outputFile, ParcelFileDescriptor.MODE_READ_WRITE)
        classUnderTest.setOutput(pfd, Muxer.MUXER_FORMAT_MPEG_4)
        // Should not throw
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInitialization_unsupportedFormatPath() {
        classUnderTest.setOutput(outputFile.absolutePath, Muxer.MUXER_FORMAT_WEBM)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInitialization_unsupportedFormatFd() {
        val pfd = ParcelFileDescriptor.open(outputFile, ParcelFileDescriptor.MODE_READ_WRITE)
        classUnderTest.setOutput(pfd, Muxer.MUXER_FORMAT_WEBM)
    }

    @Test(expected = IllegalStateException::class)
    fun testInitialization_multipleCalls_throws() {
        classUnderTest.setOutput(outputFile.absolutePath, Muxer.MUXER_FORMAT_MPEG_4)
        classUnderTest.setOutput(outputFile.absolutePath, Muxer.MUXER_FORMAT_MPEG_4)
    }

    @Test
    fun testAddTrack_missingMimeType_throws() {
        classUnderTest.setOutput(outputFile.absolutePath, Muxer.MUXER_FORMAT_MPEG_4)
        val format = MediaFormat()

        try {
            classUnderTest.addTrack(format)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("MIME type is missing") == true)
        }
    }

    @Test
    fun testAddTrack_videoMissingWidthHeight_throws() {
        classUnderTest.setOutput(outputFile.absolutePath, Muxer.MUXER_FORMAT_MPEG_4)
        val format = MediaFormat().apply {
            setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_VIDEO_AVC)
            // Missing width/height
        }

        try {
            classUnderTest.addTrack(format)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("KEY_WIDTH and KEY_HEIGHT") == true)
        }
    }

    @Test
    fun testAddTrack_audioMissingSampleRateChannels_throws() {
        classUnderTest.setOutput(outputFile.absolutePath, Muxer.MUXER_FORMAT_MPEG_4)
        val format = MediaFormat().apply {
            setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_AAC)
            // Missing sample rate/channels
        }

        try {
            classUnderTest.addTrack(format)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("KEY_SAMPLE_RATE and KEY_CHANNEL_COUNT") == true)
        }
    }

    @Test
    fun testAddTrack_videoAvcMissingCsd0_throws() {
        classUnderTest.setOutput(outputFile.absolutePath, Muxer.MUXER_FORMAT_MPEG_4)
        val format = MediaFormat().apply {
            setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_VIDEO_AVC)
            setInteger(MediaFormat.KEY_WIDTH, 1920)
            setInteger(MediaFormat.KEY_HEIGHT, 1080)
            // Missing csd-0
        }

        try {
            classUnderTest.addTrack(format)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Missing required codec-specific data (csd-0)") == true)
        }
    }

    @Test
    fun testLifecycle_valid() {
        classUnderTest.setOutput(outputFile.absolutePath, Muxer.MUXER_FORMAT_MPEG_4)

        val format = MediaFormat().apply {
            setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_VIDEO_AVC)
            setInteger(MediaFormat.KEY_WIDTH, 1280)
            setInteger(MediaFormat.KEY_HEIGHT, 720)
            setByteBuffer("csd-0", ByteBuffer.allocate(1))
        }

        val trackIndex = classUnderTest.addTrack(format)

        classUnderTest.setOrientationDegrees(90)
        classUnderTest.start()

        val buffer = ByteBuffer.allocate(10)
        val bufferInfo = MediaCodec.BufferInfo().apply {
            set(0, 10, 0, MediaCodec.BUFFER_FLAG_KEY_FRAME)
        }

        classUnderTest.writeSampleData(trackIndex, buffer, bufferInfo)

        classUnderTest.stop()
        classUnderTest.release()
    }

    @Test(expected = IllegalStateException::class)
    fun testWriteSampleData_beforeStart_throws() {
        classUnderTest.setOutput(outputFile.absolutePath, Muxer.MUXER_FORMAT_MPEG_4)

        val format = MediaFormat().apply {
            setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_VIDEO_AVC)
            setInteger(MediaFormat.KEY_WIDTH, 1280)
            setInteger(MediaFormat.KEY_HEIGHT, 720)
            setByteBuffer("csd-0", ByteBuffer.allocate(1))
        }

        val trackIndex = classUnderTest.addTrack(format)

        val buffer = ByteBuffer.allocate(10)
        val bufferInfo = MediaCodec.BufferInfo().apply {
            set(0, 10, 0, MediaCodec.BUFFER_FLAG_KEY_FRAME)
        }

        classUnderTest.writeSampleData(trackIndex, buffer, bufferInfo)
    }

    @Test(expected = IllegalStateException::class)
    fun testStop_beforeStart_throws() {
        classUnderTest.setOutput(outputFile.absolutePath, Muxer.MUXER_FORMAT_MPEG_4)
        classUnderTest.stop()
    }

    @Test
    fun testSampleBufferBounds_invalidOffset_throws() {
        classUnderTest.setOutput(outputFile.absolutePath, Muxer.MUXER_FORMAT_MPEG_4)

        val format = MediaFormat().apply {
            setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_VIDEO_AVC)
            setInteger(MediaFormat.KEY_WIDTH, 1280)
            setInteger(MediaFormat.KEY_HEIGHT, 720)
            setByteBuffer("csd-0", ByteBuffer.allocate(1))
        }

        val trackIndex = classUnderTest.addTrack(format)
        classUnderTest.start()

        val buffer = ByteBuffer.allocate(10)
        val bufferInfo = MediaCodec.BufferInfo().apply {
            set(-1, 5, 0, 0)
        }

        try {
            classUnderTest.writeSampleData(trackIndex, buffer, bufferInfo)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Negative offset") == true)
        }
    }

    @Test
    fun testSampleBufferBounds_outOfBounds_throws() {
        classUnderTest.setOutput(outputFile.absolutePath, Muxer.MUXER_FORMAT_MPEG_4)

        val format = MediaFormat().apply {
            setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_VIDEO_AVC)
            setInteger(MediaFormat.KEY_WIDTH, 1280)
            setInteger(MediaFormat.KEY_HEIGHT, 720)
            setByteBuffer("csd-0", ByteBuffer.allocate(1))
        }

        val trackIndex = classUnderTest.addTrack(format)
        classUnderTest.start()

        val buffer = ByteBuffer.allocate(10)
        val bufferInfo = MediaCodec.BufferInfo().apply {
            set(0, 15, 0, 0) // Size larger than capacity
        }

        try {
            classUnderTest.writeSampleData(trackIndex, buffer, bufferInfo)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Size out of bounds") == true)
        }
    }

    @Test
    fun testTimestamp_invalidTimestamp_throws() {
        classUnderTest.setOutput(outputFile.absolutePath, Muxer.MUXER_FORMAT_MPEG_4)

        val format = MediaFormat().apply {
            setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_VIDEO_AVC)
            setInteger(MediaFormat.KEY_WIDTH, 1280)
            setInteger(MediaFormat.KEY_HEIGHT, 720)
            setByteBuffer("csd-0", ByteBuffer.allocate(1))
        }

        val trackIndex = classUnderTest.addTrack(format)
        classUnderTest.start()

        val buffer = ByteBuffer.allocate(10)
        val bufferInfo = MediaCodec.BufferInfo().apply {
            set(0, 10, -1000, 0) // Negative presentation time
        }

        try {
            classUnderTest.writeSampleData(trackIndex, buffer, bufferInfo)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Invalid presentation time") == true)
        }
    }
}
