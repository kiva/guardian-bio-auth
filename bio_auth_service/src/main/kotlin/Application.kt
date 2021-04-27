package org.kiva.bioauthservice

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.ContentNegotiation
import io.ktor.serialization.json
import io.ktor.server.netty.EngineMain
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.app.registerApp
import org.kiva.bioauthservice.db.registerDB
import org.kiva.bioauthservice.errors.installErrorHandler
import org.kiva.bioauthservice.fingerprint.registerFingerprint
import org.kiva.bioauthservice.replay.registerReplay

@KtorExperimentalAPI
fun Application.module() {

    // Database setup
    val dbBootstrap = registerDB(log)

    // Http API middleware
    install(ContentNegotiation) {
        json()
    }
    installErrorHandler(log)

    // Register domain areas
    registerApp()
    val replayRegistry = registerReplay(log, dbBootstrap)
    registerFingerprint(replayRegistry)
}

// Main application function, which starts up Netty using the values in application.conf
fun main(args: Array<String>): Unit = EngineMain.main(args)
