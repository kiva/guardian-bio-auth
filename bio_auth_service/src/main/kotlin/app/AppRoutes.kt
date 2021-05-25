package org.kiva.bioauthservice.app

import datadog.trace.api.Trace
import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get

/*
 * Statically defined paths for these routes
 */
private const val healthzPath = "/healthz"

/*
 * Route definitions
 */
fun Route.appRoutes() {
    get(healthzPath) @Trace(operationName = healthzPath) {
        call.respondText("OK")
    }
}
