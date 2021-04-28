package org.kiva.bioauthservice.util

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
    return this.getString(path).toBoolean()
}
