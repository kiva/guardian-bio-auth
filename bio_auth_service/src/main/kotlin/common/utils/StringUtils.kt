package org.kiva.bioauthservice.common.utils

import java.util.Base64

fun String.base64ToByte(): ByteArray {
    return Base64.getDecoder().decode(this)
}
