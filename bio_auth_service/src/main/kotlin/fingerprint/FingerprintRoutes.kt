package org.kiva.bioauthservice.fingerprint

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
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

    route("/save") {
        post {
            val dto = call.receive<BulkSaveRequestDto>()
            fingerprintService.save(dto)
            call.respondText("Saved") // TODO: Should return number saved
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
