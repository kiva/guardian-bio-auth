package org.kiva.bioauthservice.common.utils

import org.apache.commons.codec.binary.Hex
import org.kiva.bioauthservice.common.errors.impl.ImageDecodeException
import java.util.Base64

/**
 * Convert a base64 encoded string to a ByteArray
 */
fun String.base64ToByte(): ByteArray {
    return Base64.getDecoder().decode(this)
}

/**
 * Convert a hex-encoded string to a ByteArray
 *
 * @throws ImageDecodeException if error happens in decoding the hex formatted image.
 */
fun String.decodeHex(): ByteArray {
    return Hex.decodeHex(this.trim().removePrefix("0x").removePrefix("\\x"))
}
