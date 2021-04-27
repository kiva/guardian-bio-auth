package org.kiva.bioauthservice

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.ContentNegotiation
import io.ktor.serialization.json
import io.ktor.server.netty.EngineMain
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.db.bootstrapDB
import org.kiva.bioauthservice.errors.registerErrorHandler
import org.kiva.bioauthservice.fingerprint.registerFingerprintRoutes
import org.kiva.bioauthservice.routes.registerAppRoutes

// Uses bootstrapDB, which itself uses an experimental API
@KtorExperimentalAPI
fun Application.module() {

    install(ContentNegotiation) {
        json()
    }

    val dbPort = bootstrapDB(log)

    registerErrorHandler(log)

    registerAppRoutes()
    registerFingerprintRoutes(log, dbPort)
}

// Main application function, which starts up Netty using the values in application.conf
fun main(args: Array<String>): Unit = EngineMain.main(args)
