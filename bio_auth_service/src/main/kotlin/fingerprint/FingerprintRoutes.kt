package org.kiva.bioauthservice.fingerprint

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.fingerprint.dtos.VerifyDto

@KtorExperimentalAPI
fun Route.fingerprintRoutes(fingerprintService: FingerprintService) {
    route("/verify") {
        post {
            val dto = call.receive<VerifyDto>()
            fingerprintService.verify(dto)
            call.respondText("Verified")
        }
    }
}
