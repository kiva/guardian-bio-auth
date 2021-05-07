package fingerprint.routes

import alphanumericStringGen
import com.typesafe.config.ConfigFactory
import fingerprint.dtos.TemplatizerDto
import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.arbitrary.next
import io.ktor.config.HoconApplicationConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.encodeToString
import org.kiva.bioauthservice.app.config.AppConfig
import org.kiva.bioauthservice.bioanalyzer.dtos.BioanalyzerReponseDto
import org.kiva.bioauthservice.common.errors.ApiError
import org.kiva.bioauthservice.common.errors.BioAuthExceptionCode
import org.kiva.bioauthservice.common.utils.base64ToString
import org.kiva.bioauthservice.db.repositories.FingerprintTemplateRepository
import org.kiva.bioauthservice.fingerprint.dtos.BulkSaveRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestFiltersDto
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestParamsDto
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition
import java.time.ZonedDateTime

@ExperimentalSerializationApi
fun List<TemplatizerDto>.serialize(): String {
    return encodeToString(ListSerializer(TemplatizerDto.serializer()), this)
}

@ExperimentalSerializationApi
fun BulkSaveRequestDto.serialize(): String {
    return encodeToString(BulkSaveRequestDto.serializer(), this)
}

@ExperimentalSerializationApi
@KtorExperimentalAPI
class FingerprintSaveRoutesSpec : WordSpec({

    // Test fixtures
    val template = this.javaClass.getResource("/images/sample_source_afis_template.txt")?.readText() ?: ""
    val image = this.javaClass.getResource("/images/sample.png")?.readBytes()?.base64ToString() ?: ""
    val appConfig = AppConfig(HoconApplicationConfig(ConfigFactory.load()))
    val bioanalyzerUrl = appConfig.bioanalyzerConfig.baseUrl + appConfig.bioanalyzerConfig.analyzePath
    val mockFingerprintTemplateRepository = mockk<FingerprintTemplateRepository>()

    beforeEach {
        clearAllMocks()
    }

    "POST /save" should {

        // TODO: Test to verify it works with foreign templates
        // TODO: Test to verify it works with different image types
        // TODO: Test to verify it fails with invalid image types

        "be able to save a template" {
            val bulkDto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        alphanumericStringGen.next(),
                        SaveRequestFiltersDto(alphanumericStringGen.next(), alphanumericStringGen.next()),
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, "", template)
                    )
                )
            )
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockk(), mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/save", bulkDto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    response.content!!.toInt() shouldBe 1
                }
            }
        }

        "be able to save a high quality image" {
            val bulkDto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        alphanumericStringGen.next(),
                        SaveRequestFiltersDto(alphanumericStringGen.next(), alphanumericStringGen.next()),
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, image)
                    )
                )
            )
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true
            val httpClient = mockHttpClient(BioAnalyzerRoute(bioanalyzerUrl, BioanalyzerReponseDto(99.0)))

            withTestApplication({
                testFingerprintRoutes(appConfig, httpClient, mockk(), mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/save", bulkDto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    response.content!!.toInt() shouldBe 1
                }
            }
        }

        "be able to save a low quality image" {
            val bulkDto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        alphanumericStringGen.next(),
                        SaveRequestFiltersDto(alphanumericStringGen.next(), alphanumericStringGen.next()),
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, image)
                    )
                )
            )
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true
            val httpClient = mockHttpClient(BioAnalyzerRoute(bioanalyzerUrl, BioanalyzerReponseDto(1.0)))

            withTestApplication({
                testFingerprintRoutes(appConfig, httpClient, mockk(), mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/save", bulkDto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    response.content!!.toInt() shouldBe 1
                }
            }
        }

        "be able to save a fingerprint with a missing code" {
            val bulkDto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        alphanumericStringGen.next(),
                        SaveRequestFiltersDto(alphanumericStringGen.next(), alphanumericStringGen.next()),
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, "", "", 0.0, "XX")
                    )
                )
            )
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockk(), mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/save", bulkDto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    response.content!!.toInt() shouldBe 1
                }
            }
        }

        "return an error if both an image and a missing code is provided" {
            val bulkDto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        alphanumericStringGen.next(),
                        SaveRequestFiltersDto(alphanumericStringGen.next(), alphanumericStringGen.next()),
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, image, "", 0.0, "XX")
                    )
                )
            )

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockk(), mockFingerprintTemplateRepository)
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
            val bulkDto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        alphanumericStringGen.next(),
                        SaveRequestFiltersDto(alphanumericStringGen.next(), alphanumericStringGen.next()),
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, "", template, 0.0, "XX")
                    )
                )
            )

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockk(), mockFingerprintTemplateRepository)
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
            val bulkDto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        alphanumericStringGen.next(),
                        SaveRequestFiltersDto(alphanumericStringGen.next(), alphanumericStringGen.next()),
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, image)
                    )
                )
            )
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true
            val httpClient = mockHttpClient(BioAnalyzerRoute(bioanalyzerUrl, null, HttpStatusCode.InternalServerError, "Error!"))

            withTestApplication({
                testFingerprintRoutes(appConfig, httpClient, mockk(), mockFingerprintTemplateRepository)
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

    "POST /templatizer/bulk/template" should {

        "be able to save a high quality image" {
            val templatizerDto = listOf(
                TemplatizerDto(
                    alphanumericStringGen.next(),
                    alphanumericStringGen.next(),
                    alphanumericStringGen.next(),
                    1,
                    FingerPosition.RIGHT_INDEX,
                    null,
                    ZonedDateTime.now(),
                    image
                )
            )
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true
            val httpClient = mockHttpClient(BioAnalyzerRoute(bioanalyzerUrl, BioanalyzerReponseDto(99.0)))

            withTestApplication({
                testFingerprintRoutes(appConfig, httpClient, mockk(), mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/templatizer/bulk/template", templatizerDto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    response.content!!.toInt() shouldBe 1
                }
            }
        }

        "be able to save a low quality image" {
            val templatizerDto = listOf(
                TemplatizerDto(
                    alphanumericStringGen.next(),
                    alphanumericStringGen.next(),
                    alphanumericStringGen.next(),
                    1,
                    FingerPosition.RIGHT_INDEX,
                    null,
                    ZonedDateTime.now(),
                    image
                )
            )
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true
            val httpClient = mockHttpClient(BioAnalyzerRoute(bioanalyzerUrl, BioanalyzerReponseDto(1.0)))

            withTestApplication({
                testFingerprintRoutes(appConfig, httpClient, mockk(), mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/templatizer/bulk/template", templatizerDto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    response.content!!.toInt() shouldBe 1
                }
            }
        }

        "be able to save a fingerprint with a missing code" {
            val templatizerDto = listOf(
                TemplatizerDto(
                    alphanumericStringGen.next(),
                    alphanumericStringGen.next(),
                    alphanumericStringGen.next(),
                    1,
                    FingerPosition.RIGHT_INDEX,
                    "XX",
                    ZonedDateTime.now()
                )
            )
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockk(), mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/templatizer/bulk/template", templatizerDto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    response.content!!.toInt() shouldBe 1
                }
            }
        }

        "return an error if both an image and a missing code is provided" {
            val templatizerDto = listOf(
                TemplatizerDto(
                    alphanumericStringGen.next(),
                    alphanumericStringGen.next(),
                    alphanumericStringGen.next(),
                    1,
                    FingerPosition.RIGHT_INDEX,
                    "XX",
                    ZonedDateTime.now(),
                    image
                )
            )
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockk(), mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/templatizer/bulk/template", templatizerDto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.BadRequest
                    response.content shouldNotBe null
                    val responseBody = Json.decodeFromString(ApiError.serializer(), response.content!!)
                    responseBody.error shouldBe BioAuthExceptionCode.BadRequestError.name
                }
            }
        }

        "return an error on saving an image if bioanalyzer returns an error" {
            val templatizerDto = listOf(
                TemplatizerDto(
                    alphanumericStringGen.next(),
                    alphanumericStringGen.next(),
                    alphanumericStringGen.next(),
                    1,
                    FingerPosition.RIGHT_INDEX,
                    null,
                    ZonedDateTime.now(),
                    image
                )
            )

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockk(), mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/templatizer/bulk/template", templatizerDto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.BadRequest
                    response.content shouldNotBe null
                    val responseBody = Json.decodeFromString(ApiError.serializer(), response.content!!)
                    responseBody.error shouldBe BioAuthExceptionCode.BioanalyzerServerError.name
                }
            }
        }
    }
})
