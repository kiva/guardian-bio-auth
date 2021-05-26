package org.kiva.bioauthservice

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.serialization.json
import io.ktor.server.netty.EngineMain
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.kiva.bioauthservice.app.registerApp
import org.kiva.bioauthservice.bioanalyzer.registerBioanalyzer
import org.kiva.bioauthservice.common.errors.installErrorHandler
import org.kiva.bioauthservice.db.registerDB
import org.kiva.bioauthservice.fingerprint.registerFingerprint
import org.kiva.bioauthservice.replay.registerReplay

@ExperimentalSerializationApi
@KtorExperimentalAPI
fun Application.module() {

    // Application setup
    val appRegistry = registerApp()
    val dbRegistry = registerDB(appRegistry)
    val replayRegistry = registerReplay(appRegistry, dbRegistry)
    val bioanalyzerRegistry = registerBioanalyzer(appRegistry)
    val fingerprintRegistry = registerFingerprint(appRegistry, dbRegistry, replayRegistry, bioanalyzerRegistry)

    // Http API middleware
    install(ContentNegotiation) {
        json()
    }
    install(DefaultHeaders)
    installErrorHandler(appRegistry)

    // Http Routes
    appRegistry.installRoutes()
    fingerprintRegistry.installRoutes()
}

// Main application function, which starts up Netty using the values in application.conf
fun main(args: Array<String>): Unit = EngineMain.main(args)
