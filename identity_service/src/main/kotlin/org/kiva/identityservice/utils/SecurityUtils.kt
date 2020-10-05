package org.kiva.identityservice.utils

import java.security.MessageDigest

/**
 * The utility functions specific for security operations.
 */

/**
 * Generates the SHA256 hash for a given string.
 *
 * @param input the input data to be hashed.
 */
fun sha256(input: String): String {
    val bytes = input.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("", { str, it -> str + "%02x".format(it) })
}

/**
 * Generates the SHA256 hash of a string and returns the first 32 characters since there's enough entropy in the first 32 chars of a SHA256 hash.
 *
 * @param input the input data to be hashed.
 */
fun sha256Half(input: String): String {
    return sha256(input).substring(0, 32)
}

/**
 * Generates hash for a given input using given pepper data.
 *
 * @param input the input data to be hashed.
 * @param pepper the pepper key used for increasing the security of hashing.
 */
fun generateHash(input: String, pepper: String?): String {
    if (pepper.isNullOrEmpty()) {
        return sha256Half(input)
    } else {
        return sha256Half(input + pepper)
    }
}

/**
 * Generates hash for a given list of inputs using given pepper data.
 *
 * @param inputList the input data to be hashed.
 * @param pepper the pepper key used for increasing the security of hashing.
 */
fun generateHashForList(inputList: List<String>, pepper: String?): List<String> {

    val returnList: MutableList<String> = mutableListOf()
    for (input in inputList) {
        if (pepper.isNullOrEmpty()) {
            returnList.add(sha256Half(input))
        } else {
            returnList.add(sha256Half(input + pepper))
        }
    }

    return returnList
}
