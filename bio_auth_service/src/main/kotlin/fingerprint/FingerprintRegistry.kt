package org.kiva.bioauthservice.fingerprint

import io.ktor.application.Application
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.kiva.bioauthservice.app.AppRegistry
import org.kiva.bioauthservice.app.config.FingerprintConfig
import org.kiva.bioauthservice.db.DbRegistry
import org.kiva.bioauthservice.replay.ReplayRegistry

@KtorExperimentalAPI
fun Application.registerFingerprint(appRegistry: AppRegistry, dbRegistry: DbRegistry, replayRegistry: ReplayRegistry): FingerprintRegistry {
    val fingerprintService = FingerprintService(
        dbRegistry.fingerprintTemplateRepository,
        appRegistry.appConfig.fingerprintConfig,
        replayRegistry.replayService
    )
    return FingerprintRegistry(this, fingerprintService)
}

@KtorExperimentalAPI
data class FingerprintRegistry(private val application: Application, val fingerprintService: FingerprintService) {
    @ExperimentalSerializationApi
    fun installRoutes() {
        application.routing {
            fingerprintRoutes(fingerprintService)
        }
    }
}
