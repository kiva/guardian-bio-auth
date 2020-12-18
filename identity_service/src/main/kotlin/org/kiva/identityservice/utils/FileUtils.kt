package org.kiva.identityservice.utils

import org.apache.commons.codec.binary.Hex
import org.apache.tika.Tika
import org.kiva.identityservice.errorhandling.exceptions.ImageDecodeException
import org.springframework.core.io.ClassPathResource
import org.springframework.util.StreamUtils
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.util.Base64
import javax.imageio.ImageIO

@Throws(IOException::class)
fun writeTempImageFile(image: ByteArray, format: String): File {
    val tmpFile = File.createTempFile("fingerprint-", "." + format.toLowerCase())
    tmpFile.deleteOnExit()
    ImageIO.write(ImageIO.read(ByteArrayInputStream(image)), format.toUpperCase(), tmpFile)
    return tmpFile
}

@Throws(IOException::class)
fun loadBytesFromResource(path: String): ByteArray {
    return StreamUtils.copyToByteArray(ClassPathResource(path).inputStream)
}

@Throws(IOException::class)
fun loadBase64FromResource(path: String): String {
    return Base64.getEncoder().encodeToString(loadBytesFromResource(path))
}

fun detectContentType(image: ByteArray): String {
    return Tika().detect(image)
}

fun base64ToByte(base64: String): ByteArray {
    return Base64.getDecoder().decode(base64)
}

/**
 * Returns the base64 of the byte array.
 *
 * @param bytes the byte array input.
 */
fun byteToBase64(bytes: ByteArray): String {
    return Base64.getEncoder().encodeToString(bytes)
}

/**
 * Decodes the wsk encoded image.
 *
 * @param imageStr the wsk encoded image.
 * @return decoded image as byte array.
 *
 * @throws ImageDecodeException if error happens in decoding the hex formatted image.
 */
fun decodeImage(imageStr: String): ByteArray {
    try {
        return Hex.decodeHex(imageStr.replace("0x", "").replace("\\x", ""))
    } catch (ex: Exception) {
        throw ImageDecodeException("There was error decoding the given hex formatted image")
    }
}
