package org.kiva.bioauthservice.routes

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing

fun Application.registerAppRoutes() {
    routing {
        appRoutes()
    }
}

fun Route.appRoutes() {
    route("/healthz") {
        get {
            call.respondText("OK")
        }
    }
}
