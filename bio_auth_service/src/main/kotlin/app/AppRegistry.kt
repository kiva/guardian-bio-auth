package org.kiva.bioauthservice.app

import io.ktor.application.Application
import io.ktor.routing.routing

fun Application.registerApp(): AppRegistry {
    return AppRegistry(this)
}

data class AppRegistry(private val application: Application) {
    fun installRoutes() {
        application.routing {
            appRoutes()
        }
    }
}
