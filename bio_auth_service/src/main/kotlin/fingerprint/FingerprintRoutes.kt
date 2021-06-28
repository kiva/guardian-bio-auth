package org.kiva.bioauthservice.fingerprint

import datadog.trace.api.Trace
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.kiva.bioauthservice.common.utils.requestIdHeader
import org.kiva.bioauthservice.fingerprint.dtos.BulkSaveRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.PositionsDto
import org.kiva.bioauthservice.fingerprint.dtos.VerifyRequestDto

/**
 * Statically defined paths for Fingerprint routes
 */
private object Paths {
    const val apiV1 = "/api/v1"
    const val getPositions = "/positions/template"
    const val save = "/save"
    const val verify = "/verify"
    const val positions = "/positions"
}

/*
 * Route definitions
 */
@ExperimentalSerializationApi
@KtorExperimentalAPI
fun Route.fingerprintRoutes(fingerprintService: FingerprintService) {

    route(Paths.apiV1) {

        post(Paths.save) @Trace(operationName = Paths.save) {
            val dto = call.receive<BulkSaveRequestDto>()
            val numSaved = fingerprintService.save(dto, call.requestIdHeader())
            call.respond(numSaved)
        }

        post(Paths.verify) @Trace(operationName = Paths.verify) {
            val dto = call.receive<VerifyRequestDto>()
            val result = fingerprintService.verify(dto, call.requestIdHeader())
            call.respond(result)
        }

        post(Paths.positions) @Trace(operationName = Paths.positions) {
            val dto = call.receive<PositionsDto>()
            val result = fingerprintService.positions(dto)
            call.respond(result)
        }
    }
}
