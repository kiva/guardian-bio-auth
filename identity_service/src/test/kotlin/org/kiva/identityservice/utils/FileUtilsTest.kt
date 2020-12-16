package org.kiva.identityservice.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.kiva.identityservice.errorhandling.exceptions.ImageDecodeException
import java.nio.file.Files
import java.util.Arrays
import java.util.Base64

class FileUtilsTest {

    private var pngFingerprint: ByteArray? = null
    private var jpgFingerprint: ByteArray? = null
    private var tiffFingerprint: ByteArray? = null

    @BeforeAll
    @Throws(Exception::class)
    fun setUp() {
        pngFingerprint = loadBytesFromResource("images/fingerprint.png")
        jpgFingerprint = loadBytesFromResource("images/fingerprint.jpg")
        tiffFingerprint = loadBytesFromResource("images/fingerprint.tif")
    }

    @Test
    @Throws(Exception::class)
    fun test_base64ToByte() {
        val encoded = Base64.getEncoder().encodeToString(pngFingerprint)
        assertTrue(Arrays.equals(pngFingerprint, base64ToByte(encoded)))
    }

    @Test
    @Throws(Exception::class)
    fun test_writeTempImageFile() {
        val file = writeTempImageFile(jpgFingerprint!!, "BMP")
        assertNotNull(file)
        assertTrue(file.exists())
        val bmpFingerprint = Files.readAllBytes(file.toPath())
        assertEquals("image/bmp", detectContentType(bmpFingerprint))
    }

    @Test
    fun test_detectContentType() {
        assertEquals("image/png", detectContentType(pngFingerprint!!))
        assertEquals("image/jpeg", detectContentType(jpgFingerprint!!))
        assertEquals("image/tiff", detectContentType(tiffFingerprint!!))
    }

    /**
     * Tests the decode image for invalid formatted image where ImageDecodeException must be thrown.
     */
    @Test
    fun testDecodeBadFormattedImage() {
        val imageStr = "Invalid_Hex_Formatted_Image"
        assertThrows<ImageDecodeException> {
            decodeImage(imageStr)
        }
    }
}
