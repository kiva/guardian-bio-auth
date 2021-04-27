package org.kiva.bioauthservice.fingerprint

import io.ktor.application.Application
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.replay.ReplayRegistry

@KtorExperimentalAPI
fun Application.registerFingerprint(replayRegistry: ReplayRegistry): FingerprintRegistry {
    val fingerprintService = FingerprintService(replayRegistry.replayService)
    routing {
        fingerprintRoutes(fingerprintService)
    }
    return FingerprintRegistry(fingerprintService)
}

@KtorExperimentalAPI
data class FingerprintRegistry(val fingerprintService: FingerprintService)
