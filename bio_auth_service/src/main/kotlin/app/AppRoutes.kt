package org.kiva.bioauthservice.app

import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.appRoutes() {
    route("/healthz") {
        get {
            call.respondText("OK")
        }
    }
}
