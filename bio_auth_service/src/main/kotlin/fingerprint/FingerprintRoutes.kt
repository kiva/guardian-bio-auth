package org.kiva.bioauthservice.fingerprint

import fingerprint.dtos.TemplatizerDto
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.kiva.bioauthservice.fingerprint.dtos.BulkSaveRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.VerifyRequestDto

@ExperimentalSerializationApi
@KtorExperimentalAPI
fun Route.fingerprintRoutes(fingerprintService: FingerprintService) {

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
}
