package org.kiva.bioauthservice.util

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
fun ApplicationConfig.getString(path: String): String {
    return this.property(path).getString()
}

@KtorExperimentalAPI
fun ApplicationConfig.getLong(path: String): Long {
    return this.getString(path).toLong(10)
}
