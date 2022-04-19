package fingerprint.routes

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.client.HttpClient
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.json
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.kiva.bioauthservice.app.AppRegistry
import org.kiva.bioauthservice.app.AppService
import org.kiva.bioauthservice.app.config.AppConfig
import org.kiva.bioauthservice.bioanalyzer.registerBioanalyzer
import org.kiva.bioauthservice.common.errors.installErrorHandler
import org.kiva.bioauthservice.db.DbRegistry
import org.kiva.bioauthservice.db.repositories.FingerprintTemplateRepository
import org.kiva.bioauthservice.db.repositories.ReplayRepository
import org.kiva.bioauthservice.fingerprint.registerFingerprint
import org.kiva.bioauthservice.replay.registerReplay

@ExperimentalSerializationApi
@KtorExperimentalAPI
fun Application.testFingerprintRoutes(
    appConfig: AppConfig,
    httpClient: HttpClient,
    replayRepository: ReplayRepository,
    fingerprintTemplateRepository: FingerprintTemplateRepository
) {
    val appService = AppService(appConfig)
    val appRegistry = AppRegistry(this, appService, appConfig, this.log, httpClient)
    val dbRegistry = DbRegistry(replayRepository, fingerprintTemplateRepository)
    val replayRegistry = this.registerReplay(appRegistry, dbRegistry)
    val bioanalyzerRegistry = this.registerBioanalyzer(appRegistry)
    val fingerprintRegistry = registerFingerprint(appRegistry, dbRegistry, replayRegistry, bioanalyzerRegistry)
    install(ContentNegotiation) {
        json()
    }
    installErrorHandler(appRegistry)
    fingerprintRegistry.installRoutes()
}

fun TestApplicationEngine.post(url: String, body: String, handleResponse: TestApplicationCall.() -> Unit) {
    handleRequest(HttpMethod.Post, url) {
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(body)
    }.apply {
        handleResponse(this)
    }
}

fun TestApplicationEngine.get(url: String, handleResponse: TestApplicationCall.() -> Unit) {
    handleRequest(HttpMethod.Get, url).apply {
        handleResponse(this)
    }
}
