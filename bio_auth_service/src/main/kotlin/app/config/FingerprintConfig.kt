package org.kiva.bioauthservice.app.config

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.common.utils.getDouble
import org.kiva.bioauthservice.common.utils.getInt

@KtorExperimentalAPI
class FingerprintConfig(baseConfig: ApplicationConfig) {
    val maxTargets = baseConfig.getInt("maxTargets")
    val matchThreshold = baseConfig.getDouble("matchThreshold")
}
