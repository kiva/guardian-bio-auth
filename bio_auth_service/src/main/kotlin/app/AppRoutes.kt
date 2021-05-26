package org.kiva.bioauthservice.app

import datadog.trace.api.Trace
import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get

/**
 * Statically defined paths for App routes
 */
private object Paths {
    const val healthz = "/healthz"
}

/*
 * Route definitions
 */
fun Route.appRoutes() {
    get(Paths.healthz) @Trace(operationName = Paths.healthz) {
        call.respondText("OK")
    }
}
