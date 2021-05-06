package org.kiva.bioauthservice.app.config

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.common.utils.getBoolean
import org.kiva.bioauthservice.common.utils.getDouble
import org.kiva.bioauthservice.common.utils.getLong
import org.kiva.bioauthservice.common.utils.getString
import java.time.Duration

@KtorExperimentalAPI
class BioanalyzerConfig(baseConfig: ApplicationConfig) {
    val enabled = baseConfig.getBoolean("enabled")
    val qualityThreshold = baseConfig.getDouble("qualityThreshold")
    val timeout = Duration.ofSeconds(baseConfig.getLong("timeout"))
    val baseUrl = baseConfig.getString("baseUrl")
    val analyzePath = baseConfig.getString("analyzePath")
}
