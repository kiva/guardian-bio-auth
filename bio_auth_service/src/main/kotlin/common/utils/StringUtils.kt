package org.kiva.bioauthservice.common.utils

import org.apache.commons.codec.binary.Hex
import java.util.Base64

/**
 * Convert a Base64-encoded String to a ByteArray
 */
fun String.base64ToByte(): ByteArray {
    return Base64.getDecoder().decode(this)
}

/**
 * Convert a ByteArray to a Base64-encoded String
 */
fun ByteArray.toBase64String(): String {
    return Base64.getEncoder().encodeToString(this)
}

/**
 * Convert a Hex-encoded String to a ByteArray
 */
fun String.hexToByte(): ByteArray {
    return Hex.decodeHex(this)
}

/**
 * Convert a ByteArray to a Hex-encoded String
 */
fun ByteArray.toHexString(): String {
    return Hex.encodeHexString(this)
}
