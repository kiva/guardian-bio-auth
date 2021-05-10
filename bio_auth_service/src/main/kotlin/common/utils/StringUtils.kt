package org.kiva.bioauthservice.common.utils

import java.util.Base64

/**
 * Convert a base64 encoded String to a ByteArray
 */
fun String.base64ToByte(): ByteArray {
    return Base64.getDecoder().decode(this)
}

/**
 * Convert a base64 encoded ByteArray to a String
 */
fun ByteArray.base64ToString(): String {
    return Base64.getEncoder().encodeToString(this)
}
