package org.kiva.bioauthservice.org.kiva.bioauthservice.app.config

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.org.kiva.bioauthservice.org.kiva.bioauthservice.app.config.ClientConfig

@KtorExperimentalAPI
class HttpConfig(baseConfig: ApplicationConfig) {
    val clientConfig = ClientConfig(baseConfig.config("client"))
}
