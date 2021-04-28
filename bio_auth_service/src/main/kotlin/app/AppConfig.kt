package org.kiva.bioauthservice.app

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.util.getString

@KtorExperimentalAPI
class AppConfig(baseConfig: ApplicationConfig) {
    val hashPepper: String = baseConfig.getString("hashPepper")
}
