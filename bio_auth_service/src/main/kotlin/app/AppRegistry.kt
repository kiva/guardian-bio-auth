package org.kiva.bioauthservice.app

import io.ktor.application.Application
import io.ktor.routing.routing
import org.kiva.bioauthservice.routes.appRoutes

fun Application.registerApp() {
    routing {
        appRoutes()
    }
}
