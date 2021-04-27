package org.kiva.bioauthservice.replay

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.util.getBoolean

@KtorExperimentalAPI
class ReplayConfig(baseConfig: ApplicationConfig) {
    val replayEnabled: Boolean = baseConfig.getBoolean("enabled")
}
