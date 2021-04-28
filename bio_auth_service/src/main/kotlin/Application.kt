package org.kiva.bioauthservice

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.ContentNegotiation
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.netty.EngineMain
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.app.registerApp
import org.kiva.bioauthservice.db.registerDB
import org.kiva.bioauthservice.errors.installErrorHandler
import org.kiva.bioauthservice.fingerprint.FingerprintRegistry
import org.kiva.bioauthservice.fingerprint.fingerprintRoutes
import org.kiva.bioauthservice.fingerprint.registerFingerprint
import org.kiva.bioauthservice.replay.registerReplay
import org.kiva.bioauthservice.app.appRoutes

@KtorExperimentalAPI
fun Application.installRoutes(fingerprintRegistry: FingerprintRegistry) {
    routing {
        appRoutes()
        fingerprintRoutes(fingerprintRegistry.fingerprintService)
    }
}

@KtorExperimentalAPI
fun Application.module() {

    // Database setup
    val dbBootstrap = registerDB(log)

    // Register domain areas
    registerApp()
    val replayRegistry = registerReplay(log, dbBootstrap)
    val fingerprintRegistry = registerFingerprint(replayRegistry)

    // Http API middleware
    install(ContentNegotiation) {
        json()
    }
    installErrorHandler(log)
    installRoutes(fingerprintRegistry)
}

// Main application function, which starts up Netty using the values in application.conf
fun main(args: Array<String>): Unit = EngineMain.main(args)
