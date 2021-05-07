package fingerprint

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath
import io.ktor.serialization.json
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.kiva.bioauthservice.app.AppRegistry
import org.kiva.bioauthservice.app.config.AppConfig
import org.kiva.bioauthservice.bioanalyzer.dtos.BioanalyzerReponseDto
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
    val appRegistry = AppRegistry(this, appConfig, this.log, httpClient)
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

fun Map<String, BioanalyzerReponseDto>.serialize(): String {
    return Json.encodeToString(MapSerializer(String.serializer(), BioanalyzerReponseDto.serializer()), this)
}

data class BioAnalyzerRoute(
    val url: String,
    val dto: BioanalyzerReponseDto? = null,
    val code: HttpStatusCode = HttpStatusCode.OK,
    val errMsg: String = ""
)

fun mockHttpClient(route: BioAnalyzerRoute): HttpClient {
    return HttpClient(MockEngine) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }
            )
        }
        engine {
            addHandler {
                val reqId = it.headers[HttpHeaders.XRequestId] ?: "noRequestId"
                when (it.url.toString()) {
                    route.url -> {
                        val responseBody = if (route.dto == null) {
                            route.errMsg
                        } else {
                            mapOf(Pair(reqId, route.dto)).serialize()
                        }
                        respond(responseBody, route.code)
                    }
                    else -> error("Unhandled ${it.url.fullPath}")
                }
            }
        }
    }
}
