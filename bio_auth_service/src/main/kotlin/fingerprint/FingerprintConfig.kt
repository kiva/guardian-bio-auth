package org.kiva.bioauthservice.fingerprint

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.util.getInt

@KtorExperimentalAPI
class FingerprintConfig(baseConfig: ApplicationConfig) {
    val maxDids: Int = baseConfig.getInt("maxDids")
}
