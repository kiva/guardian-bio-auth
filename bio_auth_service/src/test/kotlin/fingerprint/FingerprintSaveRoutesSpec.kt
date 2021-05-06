package fingerprint

import com.typesafe.config.ConfigFactory
import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.config.HoconApplicationConfig
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
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.encodeToString
import org.kiva.bioauthservice.app.AppRegistry
import org.kiva.bioauthservice.app.config.AppConfig
import org.kiva.bioauthservice.bioanalyzer.dtos.BioanalyzerReponseDto
import org.kiva.bioauthservice.bioanalyzer.registerBioanalyzer
import org.kiva.bioauthservice.common.errors.ApiError
import org.kiva.bioauthservice.common.errors.BioAuthExceptionCode
import org.kiva.bioauthservice.common.errors.impl.BioanalyzerServiceException
import org.kiva.bioauthservice.common.errors.installErrorHandler
import org.kiva.bioauthservice.common.utils.base64ToString
import org.kiva.bioauthservice.db.DbRegistry
import org.kiva.bioauthservice.db.daos.ReplayDao
import org.kiva.bioauthservice.db.repositories.FingerprintTemplateRepository
import org.kiva.bioauthservice.db.repositories.ReplayRepository
import org.kiva.bioauthservice.fingerprint.dtos.BulkSaveRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestFiltersDto
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestParamsDto
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition
import org.kiva.bioauthservice.fingerprint.registerFingerprint
import org.kiva.bioauthservice.replay.registerReplay
import java.time.ZonedDateTime

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

@ExperimentalSerializationApi
fun BulkSaveRequestDto.serialize(): String {
    return encodeToString(BulkSaveRequestDto.serializer(), this)
}

fun Map<String, BioanalyzerReponseDto>.serialize(): String {
    return encodeToString(MapSerializer(String.serializer(), BioanalyzerReponseDto.serializer()), this)
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
            serializer = KotlinxSerializer(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
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

fun TestApplicationEngine.post(url: String, body: String, handleResponse: TestApplicationCall.() -> Unit) {
    handleRequest(HttpMethod.Post, url) {
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(body)
    }.apply {
        handleResponse(this)
    }
}

@ExperimentalSerializationApi
@KtorExperimentalAPI
class FingerprintSaveRoutesSpec : WordSpec({

    // Test fixtures
    val alphanumericStringGen = Arb.string(10, Arb.alphanumeric())
    val template = this.javaClass.getResource("/images/sample_template.txt")?.readText() ?: ""
    val image = this.javaClass.getResource("/images/sample.png")?.readBytes()?.base64ToString() ?: ""
    val appConfig = AppConfig(HoconApplicationConfig(ConfigFactory.load()))
    val bioanalyzerUrl = appConfig.bioanalyzerConfig.baseUrl + appConfig.bioanalyzerConfig.analyzePath
    val mockReplayRepository = mockk<ReplayRepository>()
    val mockFingerprintTemplateRepository = mockk<FingerprintTemplateRepository>()

    "POST /save" should {

        "be able to save a template" {
            val bulkDto = BulkSaveRequestDto(listOf(SaveRequestDto(
                alphanumericStringGen.next(),
                SaveRequestFiltersDto(alphanumericStringGen.next(), alphanumericStringGen.next()),
                SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, "", template)
            )))
            every { mockReplayRepository.addReplay(any()) } returns ReplayDao(1, "foo", ZonedDateTime.now(), 1)
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockReplayRepository, mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/save", bulkDto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    response.content!!.toInt() shouldBe 1
                }
            }
        }

        "be able to save a high quality image" {
            val bulkDto = BulkSaveRequestDto(listOf(SaveRequestDto(
                alphanumericStringGen.next(),
                SaveRequestFiltersDto(alphanumericStringGen.next(), alphanumericStringGen.next()),
                SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, image)
            )))
            every { mockReplayRepository.addReplay(any()) } returns ReplayDao(1, "foo", ZonedDateTime.now(), 1)
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true
            val httpClient = mockHttpClient(BioAnalyzerRoute(bioanalyzerUrl, BioanalyzerReponseDto(99.0)))

            withTestApplication({
                testFingerprintRoutes(appConfig, httpClient, mockReplayRepository, mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/save", bulkDto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    response.content!!.toInt() shouldBe 1
                }
            }
        }

        "be able to save a low quality image" {
            val bulkDto = BulkSaveRequestDto(listOf(SaveRequestDto(
                alphanumericStringGen.next(),
                SaveRequestFiltersDto(alphanumericStringGen.next(), alphanumericStringGen.next()),
                SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, image)
            )))
            every { mockReplayRepository.addReplay(any()) } returns ReplayDao(1, "foo", ZonedDateTime.now(), 1)
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true
            val httpClient = mockHttpClient(BioAnalyzerRoute(bioanalyzerUrl, BioanalyzerReponseDto(1.0)))

            withTestApplication({
                testFingerprintRoutes(appConfig, httpClient, mockReplayRepository, mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/save", bulkDto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    response.content!!.toInt() shouldBe 1
                }
            }
        }

        "be able to save a fingerprint with a missing code" {
            val bulkDto = BulkSaveRequestDto(listOf(SaveRequestDto(
                alphanumericStringGen.next(),
                SaveRequestFiltersDto(alphanumericStringGen.next(), alphanumericStringGen.next()),
                SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, "", "", 0.0, "XX")
            )))
            every { mockReplayRepository.addReplay(any()) } returns ReplayDao(1, "foo", ZonedDateTime.now(), 1)
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockReplayRepository, mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/save", bulkDto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    response.content!!.toInt() shouldBe 1
                }
            }
        }

        "return an error if both an image and a missing code is provided" {
            val bulkDto = BulkSaveRequestDto(listOf(SaveRequestDto(
                alphanumericStringGen.next(),
                SaveRequestFiltersDto(alphanumericStringGen.next(), alphanumericStringGen.next()),
                SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, image, "", 0.0, "XX")
            )))

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockReplayRepository, mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/save", bulkDto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.BadRequest
                    response.content shouldNotBe null
                    val responseBody = Json.decodeFromString(ApiError.serializer(), response.content!!)
                    responseBody.error shouldBe BioAuthExceptionCode.BadRequestError.name
                }
            }
        }

        "return an error if both a template and a missing code is provided" {
            val bulkDto = BulkSaveRequestDto(listOf(SaveRequestDto(
                alphanumericStringGen.next(),
                SaveRequestFiltersDto(alphanumericStringGen.next(), alphanumericStringGen.next()),
                SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, "", template, 0.0, "XX")
            )))

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockReplayRepository, mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/save", bulkDto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.BadRequest
                    response.content shouldNotBe null
                    val responseBody = Json.decodeFromString(ApiError.serializer(), response.content!!)
                    responseBody.error shouldBe BioAuthExceptionCode.BadRequestError.name
                }
            }
        }

        "return an error on saving an image if bioanalyzer returns an error" {
            val bulkDto = BulkSaveRequestDto(listOf(SaveRequestDto(
                alphanumericStringGen.next(),
                SaveRequestFiltersDto(alphanumericStringGen.next(), alphanumericStringGen.next()),
                SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, image)
            )))
            every { mockReplayRepository.addReplay(any()) } returns ReplayDao(1, "foo", ZonedDateTime.now(), 1)
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true
            val httpClient = mockHttpClient(BioAnalyzerRoute(bioanalyzerUrl, null, HttpStatusCode.InternalServerError, "Error!"))

            withTestApplication({
                testFingerprintRoutes(appConfig, httpClient, mockReplayRepository, mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/save", bulkDto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.BadRequest
                    response.content shouldNotBe null
                    val responseBody = Json.decodeFromString(ApiError.serializer(), response.content!!)
                    responseBody.error shouldBe BioAuthExceptionCode.BioanalyzerServerError.name
                }
            }
        }
    }
})