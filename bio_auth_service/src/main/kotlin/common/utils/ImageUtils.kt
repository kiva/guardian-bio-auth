package org.kiva.bioauthservice.common.utils

import org.apache.tika.Tika

fun ByteArray.detectContentType(): String {
    return Tika().detect(this)
}
