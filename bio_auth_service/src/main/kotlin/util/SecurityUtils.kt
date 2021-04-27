package org.kiva.bioauthservice.util

import java.security.MessageDigest

fun ByteArray.toSha512(): String {
    val md = MessageDigest.getInstance("SHA-512")
    val digest = md.digest(this)
    return digest.fold("", { str, it -> str + "%02x".format(it) })
}
