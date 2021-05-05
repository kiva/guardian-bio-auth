package org.kiva.bioauthservice.common.utils

import java.util.Base64

/**
 * Convert a base64 encoded string to a ByteArray
 */
fun String.base64ToByte(): ByteArray {
    return Base64.getDecoder().decode(this)
}
