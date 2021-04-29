package org.kiva.bioauthservice.common.utils

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
fun ApplicationConfig.getString(path: String): String {
    return this.property(path).getString()
}

@KtorExperimentalAPI
fun ApplicationConfig.getInt(path: String): Int {
    return this.getString(path).toInt(10)
}

@KtorExperimentalAPI
fun ApplicationConfig.getLong(path: String): Long {
    return this.getString(path).toLong(10)
}

@KtorExperimentalAPI
fun ApplicationConfig.getBoolean(path: String): Boolean {
    val result = this.getString(path)
    if (result.toLowerCase() == "true") {
        return true
    } else if (result.toLowerCase() == "false") {
        return false
    } else {
        throw IllegalArgumentException("$result is not a true/false boolean value")
    }
}
