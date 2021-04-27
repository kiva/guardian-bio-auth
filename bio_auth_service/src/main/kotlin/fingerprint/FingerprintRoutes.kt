package org.kiva.bioauthservice.fingerprint

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import org.kiva.bioauthservice.db.DbPort
import org.kiva.bioauthservice.fingerprint.dtos.VerifyDto
import org.slf4j.Logger

fun Application.registerFingerprintRoutes(logger: Logger, dbPort: DbPort) {
    val fingerprintService = FingerprintService(logger, dbPort)
    routing {
        fingerprintRoutes(fingerprintService)
    }
}

fun Route.fingerprintRoutes(fingerprintService: FingerprintService) {
    route("/verify") {
        post {
            val dto = call.receive<VerifyDto>()
            fingerprintService.verify(dto)
            call.respondText("Verified")
        }
    }
}
