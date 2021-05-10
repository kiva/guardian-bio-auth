package org.kiva.bioauthservice.app

import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get

fun Route.appRoutes() {
    get("/healthz") {
        call.respondText("OK")
    }
}
