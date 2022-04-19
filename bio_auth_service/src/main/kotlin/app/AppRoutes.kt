package org.kiva.bioauthservice.app

import datadog.trace.api.Trace
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.ExperimentalSerializationApi

/**
 * Statically defined paths for App routes
 */
private object Paths {
    const val healthz = "/healthz"
    const val stats = "/stats"
}

/*
 * Route definitions
 */
@ExperimentalSerializationApi
@KtorExperimentalAPI
fun Route.appRoutes(appService: AppService) {

    get(Paths.healthz) @Trace(operationName = Paths.healthz) {
        call.respondText("OK")
    }

    get(Paths.stats) @Trace(operationName = Paths.stats) {
        call.respond(appService.getStats())
    }
}
