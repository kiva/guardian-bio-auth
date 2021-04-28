package org.kiva.bioauthservice.fingerprint

import io.ktor.application.Application
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.replay.ReplayRegistry

@KtorExperimentalAPI
fun Application.registerFingerprint(replayRegistry: ReplayRegistry): FingerprintRegistry {
    val baseConfig = environment.config.config("fingerprint")
    val fingerprintConfig = FingerprintConfig(baseConfig)
    val fingerprintService = FingerprintService(replayRegistry.replayService, fingerprintConfig)
    return FingerprintRegistry(fingerprintService)
}

@KtorExperimentalAPI
data class FingerprintRegistry(val fingerprintService: FingerprintService)
