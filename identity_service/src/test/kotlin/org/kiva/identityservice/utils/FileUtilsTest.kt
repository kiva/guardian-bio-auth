package org.kiva.identityservice.utils

import java.nio.file.Files
import java.util.Arrays
import java.util.Base64
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.kiva.identityservice.errorhandling.exceptions.ImageDecodeException

class FileUtilsTest {

    private var pngFingerprint: ByteArray? = null
    private var jpgFingerprint: ByteArray? = null
    private var tiffFingerprint: ByteArray? = null

    @Before
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
        assertThat(file, `is`(not(nullValue())))
        assertTrue(file.exists())
        val bmpFingerprint = Files.readAllBytes(file.toPath())
        assertThat(detectContentType(bmpFingerprint), `is`("image/bmp"))
    }

    @Test
    fun test_detectContentType() {
        assertThat(detectContentType(pngFingerprint!!), `is`("image/png"))
        assertThat(detectContentType(jpgFingerprint!!), `is`("image/jpeg"))
        assertThat(detectContentType(tiffFingerprint!!), `is`("image/tiff"))
    }

    /**
     * Tests the decode image for invalid formatted image where ImageDecodeException must be thrown.
     */
    @Test(expected = ImageDecodeException::class)
    fun testDecodeBadFormattedImage() {
        val imageStr = "Invalid_Hex_Formatted_Image"
        decodeImage(imageStr)
        Assert.fail("Should not run this code!")
    }
}
