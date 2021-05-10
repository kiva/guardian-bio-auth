package org.kiva.bioauthservice.app.config

import io.ktor.client.features.logging.LogLevel
import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.common.utils.getLong
import org.kiva.bioauthservice.common.utils.getString

@KtorExperimentalAPI
class ClientConfig(baseConfig: ApplicationConfig) {
    val logLevel = LogLevel.valueOf(baseConfig.getString("logLevel"))
    val connectTimeoutMillis = baseConfig.getLong("connectTimeoutMillis")
    val socketTimeoutMillis = baseConfig.getLong("socketTimeoutMillis")
    val requestTimeoutMillis = baseConfig.getLong("requestTimeoutMillis")
}
