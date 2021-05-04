package org.kiva.bioauthservice.app

import io.ktor.application.Application
import io.ktor.application.log
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.app.config.AppConfig
import org.slf4j.Logger

@KtorExperimentalAPI
fun Application.registerApp(): AppRegistry {
    val appConfig = AppConfig(environment.config)
    return AppRegistry(this, appConfig, log)
}

@KtorExperimentalAPI
data class AppRegistry(private val application: Application, val appConfig: AppConfig, val logger: Logger) {
    fun installRoutes() {
        application.routing {
            appRoutes()
        }
    }
}
