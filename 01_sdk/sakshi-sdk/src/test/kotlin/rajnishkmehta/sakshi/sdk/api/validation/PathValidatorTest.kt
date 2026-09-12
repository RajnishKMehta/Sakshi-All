package rajnishkmehta.sakshi.sdk.api.validation

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.File
import org.junit.jupiter.api.Assertions.assertTrue

public class PathValidatorTest {

    @Test
    public fun testValidMediaType() {
        PathValidator.validateMediaType("PHOTO")
        PathValidator.validateMediaType("VIDEO")
        PathValidator.validateMediaType("AUDIO")
        PathValidator.validateMediaType("OTHER")
    }

    @Test
    public fun testInvalidMediaType() {
        assertThrows(IllegalArgumentException::class.java) { PathValidator.validateMediaType("DOCUMENT") }
        assertThrows(IllegalArgumentException::class.java) { PathValidator.validateMediaType("") }
        assertThrows(IllegalArgumentException::class.java) { PathValidator.validateMediaType("../PHOTO") }
    }

    @Test
    public fun testValidFileId() {
        PathValidator.validateFileId("vid-123_456")
        PathValidator.validateFileId("1234567890")
    }

    @Test
    public fun testInvalidFileId() {
        assertThrows(IllegalArgumentException::class.java) { PathValidator.validateFileId("") }
        assertThrows(IllegalArgumentException::class.java) { PathValidator.validateFileId("Vid-123") }
        assertThrows(IllegalArgumentException::class.java) { PathValidator.validateFileId("vid/123") }
        assertThrows(IllegalArgumentException::class.java) { PathValidator.validateFileId("vid..123") }
        assertThrows(IllegalArgumentException::class.java) { PathValidator.validateFileId("vid 123") }
        assertThrows(IllegalArgumentException::class.java) { PathValidator.validateFileId("short") }
        assertThrows(IllegalArgumentException::class.java) { PathValidator.validateFileId("a".repeat(129)) }
    }

    @Test
    public fun testValidFileExtension() {
        PathValidator.validateFileExtension("mp4")
        PathValidator.validateFileExtension("tar.gz")
        PathValidator.validateFileExtension("jpg")
    }

    @Test
    public fun testInvalidFileExtension() {
        assertThrows(IllegalArgumentException::class.java) { PathValidator.validateFileExtension("") }
        assertThrows(IllegalArgumentException::class.java) { PathValidator.validateFileExtension(".mp4") }
        assertThrows(IllegalArgumentException::class.java) { PathValidator.validateFileExtension("mp4.") }
        assertThrows(IllegalArgumentException::class.java) { PathValidator.validateFileExtension("mp4/avi") }
        assertThrows(IllegalArgumentException::class.java) { PathValidator.validateFileExtension("..") }
        assertThrows(IllegalArgumentException::class.java) { PathValidator.validateFileExtension("a".repeat(13)) }
    }

    @Test
    public fun testValidDestinationPath() {
        val root = File("/tmp/media")
        val file = File("/tmp/media/photo/test.jpg")
        // Should not throw
        PathValidator.validateDestinationPath(root, file)
    }

    @Test
    public fun testInvalidDestinationPath() {
        val root = File("/tmp/media")
        val file1 = File("/tmp/other/test.jpg")
        assertThrows(SecurityException::class.java) { PathValidator.validateDestinationPath(root, file1) }

        // Traversal
        val file2 = File(root, "../../etc/passwd")
        assertThrows(SecurityException::class.java) { PathValidator.validateDestinationPath(root, file2) }
    }
}
