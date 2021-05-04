package org.kiva.bioauthservice.app.config

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.common.utils.getBoolean

@KtorExperimentalAPI
class ReplayConfig(baseConfig: ApplicationConfig) {
    val replayEnabled: Boolean = baseConfig.getBoolean("enabled")
}
