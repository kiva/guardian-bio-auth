package org.kiva.bioauthservice.fingerprint

import common.errors.impl.InvalidFilterException
import datadog.trace.api.Trace
import fingerprint.dtos.TemplatizerDto
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

/*
 * Statically defined paths for these routes
 */
private const val templatizerPath = "/templatizer/bulk/template"
private const val getPositionsPath = "/positions/template"
private const val savePath = "/save"
private const val verifyPath = "/verify"
private const val positionsPath = "/positions"

/*
 * Route definitions
 */
@ExperimentalSerializationApi
@KtorExperimentalAPI
fun Route.fingerprintRoutes(fingerprintService: FingerprintService) {

    route("/api/v1") {

        // TODO: Remove this deprecated route. Use POST /save instead
        post(templatizerPath) @Trace(operationName = templatizerPath) {
            val dtos = call.receive<List<TemplatizerDto>>()
            val bulkSaveDto = BulkSaveRequestDto(dtos.map { it.toSaveRequestDto() })
            val numSaved = fingerprintService.save(bulkSaveDto, call.requestIdHeader())
            call.respond(numSaved)
        }

        // TODO: Remove this deprecated route. Use POST /positions instead
        get("$getPositionsPath/{filter}") @Trace(operationName = getPositionsPath) {
            val filters = call.parameters["filter"]?.split("=") ?: emptyList()
            if (filters.size != 2) {
                throw InvalidFilterException("One of your filters is invalid or missing. Filter has to be in the format 'national_id=123'")
            }
            val dto = when (filters[0]) {
                "nationalId" -> PositionsDto(filters[1])
                "voterId" -> PositionsDto(null, filters[1])
                "dids" -> PositionsDto(null, null, filters[1])
                else -> throw InvalidFilterException("${filters[0]} is an invalid filter type")
            }
            val result = fingerprintService.positions(dto)
            call.respond(result)
        }

        post(savePath) @Trace(operationName = savePath) {
            val dto = call.receive<BulkSaveRequestDto>()
            val numSaved = fingerprintService.save(dto, call.requestIdHeader())
            call.respond(numSaved)
        }

        post(verifyPath) @Trace(operationName = verifyPath) {
            val dto = call.receive<VerifyRequestDto>()
            val result = fingerprintService.verify(dto, call.requestIdHeader())
            call.respond(result)
        }

        post(positionsPath) @Trace(operationName = positionsPath) {
            val dto = call.receive<PositionsDto>()
            val result = fingerprintService.positions(dto)
            call.respond(result)
        }
    }
}
