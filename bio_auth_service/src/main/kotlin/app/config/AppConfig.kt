package org.kiva.bioauthservice.app.config

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.common.utils.getString

@KtorExperimentalAPI
class AppConfig(baseConfig: ApplicationConfig) {

    // Top-level Info
    val name = baseConfig.getString("name")

    // Sub-Configs
    val bioanalyzerConfig = BioanalyzerConfig(baseConfig.config("bioanalyzer"))
    val dbConfig = DbConfig(baseConfig.config("db"))
    val httpConfig = HttpConfig(baseConfig.config("http"))
    val fingerprintConfig = FingerprintConfig(baseConfig.config("fingerprint"))
    val replayConfig = ReplayConfig(baseConfig.config("replay"))
}
