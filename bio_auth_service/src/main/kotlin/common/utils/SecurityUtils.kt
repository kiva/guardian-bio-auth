package org.kiva.bioauthservice.common.utils

import java.security.MessageDigest

/**
 * Generates a SHA512 hash for a given ByteArray
 */
fun ByteArray.toSha512(): String {
    val md = MessageDigest.getInstance("SHA-512")
    val digest = md.digest(this)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}

/**
 * Generates the SHA256 hash for a given string.
 */
fun String.sha256(): String {
    val bytes = this.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}

/**
 * Generates the SHA256 hash of a string and returns the first 32 characters since there's enough entropy in the first 32 chars of a SHA256 hash.
 */
fun String.sha256Half(): String {
    return this.sha256().substring(0, 32)
}

/**
 * Generates hash for a given input using given pepper data.
 *
 * @param pepper the pepper key used for increasing the security of hashing.
 */
fun String.generateHash(pepper: String): String {
    return if (pepper.isEmpty()) {
        this.sha256Half()
    } else {
        (this + pepper).sha256Half()
    }
}

/**
 * Generates hash for a given list of inputs using given pepper data.
 *
 * @param pepper the pepper key used for increasing the security of hashing.
 */
fun List<String>.generateHashForList(pepper: String): List<String> {
    return this.map { it.generateHash(pepper) }
}
