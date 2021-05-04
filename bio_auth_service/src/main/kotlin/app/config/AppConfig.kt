package org.kiva.bioauthservice.app.config

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
class AppConfig(baseConfig: ApplicationConfig) {
    val dbConfig: DbConfig = DbConfig(baseConfig.config("db"))
    val fingerprintConfig: FingerprintConfig = FingerprintConfig(baseConfig.config("fingerprint"))
    val replayConfig: ReplayConfig = ReplayConfig(baseConfig.config("replay"))
}