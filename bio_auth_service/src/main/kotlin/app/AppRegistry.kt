package org.kiva.bioauthservice.app

import io.ktor.application.Application
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
fun Application.registerApp(): AppRegistry {
    val baseConfig = environment.config.config("replay")
    val appConfig = AppConfig(baseConfig)
    return AppRegistry(appConfig)
}

@KtorExperimentalAPI
data class AppRegistry(val appConfig: AppConfig)
