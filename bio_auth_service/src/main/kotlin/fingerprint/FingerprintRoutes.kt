package org.kiva.bioauthservice.fingerprint

import common.errors.impl.InvalidFilterException
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
import org.kiva.bioauthservice.fingerprint.dtos.BulkSaveRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.PositionsDto
import org.kiva.bioauthservice.fingerprint.dtos.VerifyRequestDto

@ExperimentalSerializationApi
@KtorExperimentalAPI
fun Route.fingerprintRoutes(fingerprintService: FingerprintService) {

    route("/api/v1") {
        route("/templatizer/bulk/template") {
            post {
                val dtos = call.receive<List<TemplatizerDto>>()
                val bulkSaveDto = BulkSaveRequestDto(dtos.map { it.toSaveRequestDto() })
                val numSaved = fingerprintService.save(bulkSaveDto)
                call.respond(numSaved)
            }
        }

        route("/save") {
            post {
                val dto = call.receive<BulkSaveRequestDto>()
                val numSaved = fingerprintService.save(dto)
                call.respond(numSaved)
            }
        }

        route("/verify") {
            post {
                val dto = call.receive<VerifyRequestDto>()
                val result = fingerprintService.verify(dto)
                call.respond(result)
            }
        }

        route("/positions/template/{filter}") {
            get {
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
        }
    }
}
