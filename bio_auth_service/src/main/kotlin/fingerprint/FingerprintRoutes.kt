package org.kiva.bioauthservice.fingerprint

import common.errors.impl.InvalidFilterException
import fingerprint.dtos.TemplatizerDto
import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.kiva.bioauthservice.fingerprint.dtos.BulkSaveRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.PositionsDto
import org.kiva.bioauthservice.fingerprint.dtos.VerifyRequestDto

@ExperimentalSerializationApi
@KtorExperimentalAPI
fun Route.fingerprintRoutes(fingerprintService: FingerprintService) {

    route("/api/v1") {

        // TODO: Remove this deprecated route. Use POST /save instead
        post("/templatizer/bulk/template") {
            val dtos = call.receive<List<TemplatizerDto>>()
            val requestId = call.request.header(HttpHeaders.XRequestId) ?: "noRequestId"
            val bulkSaveDto = BulkSaveRequestDto(dtos.map { it.toSaveRequestDto() })
            val numSaved = fingerprintService.save(bulkSaveDto, requestId)
            call.respond(numSaved)
        }

        // TODO: Remove this deprecated route. Use POST /positions instead
        get("/positions/template/{filter}") {
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

        post("/save") {
            val dto = call.receive<BulkSaveRequestDto>()
            val requestId = call.request.header(HttpHeaders.XRequestId) ?: "noRequestId"
            val numSaved = fingerprintService.save(dto, requestId)
            call.respond(numSaved)
        }

        post("/verify") {
            val dto = call.receive<VerifyRequestDto>()
            val requestId = call.request.header(HttpHeaders.XRequestId) ?: "noRequestId"
            val result = fingerprintService.verify(dto, requestId)
            call.respond(result)
        }

        post("/positions") {
            val dto = call.receive<PositionsDto>()
            val result = fingerprintService.positions(dto)
            call.respond(result)
        }
    }
}
