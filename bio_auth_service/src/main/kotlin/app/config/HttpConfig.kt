package org.kiva.bioauthservice.app.config

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class HttpConfig(baseConfig: ApplicationConfig) {
    val clientConfig = ClientConfig(baseConfig.config("client"))
}
