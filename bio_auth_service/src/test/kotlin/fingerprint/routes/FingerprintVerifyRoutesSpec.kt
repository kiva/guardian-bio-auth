package fingerprint.routes

import BioAnalyzerRoute
import alphanumericStringGen
import com.machinezoo.sourceafis.FingerprintTemplate
import com.typesafe.config.ConfigFactory
import fingerprint.dtos.VerifyResponseDto
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
import kotlinx.serialization.json.Json
import mockHttpClient
import org.kiva.bioauthservice.app.config.AppConfig
import org.kiva.bioauthservice.bioanalyzer.dtos.BioanalyzerReponseDto
import org.kiva.bioauthservice.common.errors.ApiError
import org.kiva.bioauthservice.common.errors.BioAuthExceptionCode
import org.kiva.bioauthservice.common.utils.base64ToByte
import org.kiva.bioauthservice.common.utils.toBase64String
import org.kiva.bioauthservice.common.utils.toHexString
import org.kiva.bioauthservice.db.daos.FingerprintTemplateDao
import org.kiva.bioauthservice.db.daos.ReplayDao
import org.kiva.bioauthservice.db.repositories.FingerprintTemplateRepository
import org.kiva.bioauthservice.db.repositories.ReplayRepository
import org.kiva.bioauthservice.fingerprint.dtos.VerifyRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.VerifyRequestFiltersDto
import org.kiva.bioauthservice.fingerprint.dtos.VerifyRequestParamsDto
import org.kiva.bioauthservice.fingerprint.enums.DataType
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition
import org.kiva.bioauthservice.fingerprint.enums.ResponseStatus
import java.time.ZonedDateTime

fun VerifyRequestDto.serialize(): String {
    return Json.encodeToString(VerifyRequestDto.serializer(), this)
}

@ExperimentalSerializationApi
@KtorExperimentalAPI
class FingerprintVerifyRoutesSpec : WordSpec({

    // Test fixtures
    val agentId = alphanumericStringGen.next()
    val position = FingerPosition.RIGHT_INDEX
    val sourceAfisTemplateString = this.javaClass.getResource("/images/sample_source_afis_template.txt")?.readText() ?: ""
    val ansi378v2004Template = this.javaClass.getResource("/images/sample_ansi_378_2004_template.txt")?.readText() ?: ""
    val ansi378v2009Template = this.javaClass.getResource("/images/sample_ansi_378_2009_template.txt")?.readText() ?: ""
    val base64Image = this.javaClass.getResource("/images/sample.jpg")?.readBytes()?.toBase64String() ?: ""
    val hexImage = this.javaClass.getResource("/images/sample.jpg")?.readBytes()?.toHexString() ?: ""
    val gifImage = this.javaClass.getResource("/images/sample.gif")?.readBytes()?.toBase64String() ?: ""
    val otherImage = this.javaClass.getResource("/images/sample.png")?.readBytes()?.toBase64String() ?: "" // Not the same fingerprint
    val sourceAfisTemplate = FingerprintTemplate(sourceAfisTemplateString.base64ToByte())
    val appConfig = AppConfig(HoconApplicationConfig(ConfigFactory.load()))
    val bioanalyzerUrl = appConfig.bioanalyzerConfig.baseUrl + appConfig.bioanalyzerConfig.analyzePath
    val mockReplayRepository = mockk<ReplayRepository>()
    val mockFingerprintTemplateRepository = mockk<FingerprintTemplateRepository>()
    val dao = FingerprintTemplateDao(
        1,
        agentId,
        1,
        3,
        position,
        "sourceafis",
        sourceAfisTemplate,
        null,
        90,
        ZonedDateTime.now(),
        ZonedDateTime.now(),
        ZonedDateTime.now()
    )

    beforeEach {
        clearAllMocks()
    }

    "POST /verify" should {

        "be able to verify a base64-encoded image against an existing template" {
            val dtoFilters = VerifyRequestFiltersDto(agentId)
            val dtoParams = VerifyRequestParamsDto(base64Image, position)
            val dto = VerifyRequestDto(dtoFilters, dtoParams)
            every { mockReplayRepository.addReplay(any()) } returns ReplayDao(1, "foo", ZonedDateTime.now(), 1)
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(dao)

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockReplayRepository, mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/verify", dto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    val responseBody = Json.decodeFromString(VerifyResponseDto.serializer(), response.content!!)
                    responseBody.status shouldBe ResponseStatus.MATCHED
                    responseBody.agentId shouldBe agentId
                }
            }
        }

        "be able to verify a hex-encoded image against an existing template" {
            val dtoFilters = VerifyRequestFiltersDto(agentId)
            val dtoParams = VerifyRequestParamsDto(hexImage, position)
            val dto = VerifyRequestDto(dtoFilters, dtoParams)
            every { mockReplayRepository.addReplay(any()) } returns ReplayDao(1, "foo", ZonedDateTime.now(), 1)
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(dao)

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockReplayRepository, mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/verify", dto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    val responseBody = Json.decodeFromString(VerifyResponseDto.serializer(), response.content!!)
                    responseBody.status shouldBe ResponseStatus.MATCHED
                    responseBody.agentId shouldBe agentId
                }
            }
        }

        "be able to verify a .gif image against an existing template" {
            val dtoFilters = VerifyRequestFiltersDto(agentId)
            val dtoParams = VerifyRequestParamsDto(gifImage, position)
            val dto = VerifyRequestDto(dtoFilters, dtoParams)
            every { mockReplayRepository.addReplay(any()) } returns ReplayDao(1, "foo", ZonedDateTime.now(), 1)
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(dao)

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockReplayRepository, mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/verify", dto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    val responseBody = Json.decodeFromString(VerifyResponseDto.serializer(), response.content!!)
                    responseBody.status shouldBe ResponseStatus.MATCHED
                    responseBody.agentId shouldBe agentId
                }
            }
        }

        "be able to verify a Source AFIS v3 template against an existing template" {
            val dtoFilters = VerifyRequestFiltersDto(agentId)
            val dtoParams = VerifyRequestParamsDto(sourceAfisTemplateString, position)
            val dto = VerifyRequestDto(dtoFilters, dtoParams, DataType.TEMPLATE)
            every { mockReplayRepository.addReplay(any()) } returns ReplayDao(1, "foo", ZonedDateTime.now(), 1)
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(dao)

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockReplayRepository, mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/verify", dto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    val responseBody = Json.decodeFromString(VerifyResponseDto.serializer(), response.content!!)
                    responseBody.status shouldBe ResponseStatus.MATCHED
                    responseBody.agentId shouldBe agentId
                }
            }
        }

        "be able to verify an ANSI-378-2004 template against an existing template" {
            val dtoFilters = VerifyRequestFiltersDto(agentId)
            val dtoParams = VerifyRequestParamsDto(ansi378v2004Template, position)
            val dto = VerifyRequestDto(dtoFilters, dtoParams, DataType.TEMPLATE)
            every { mockReplayRepository.addReplay(any()) } returns ReplayDao(1, "foo", ZonedDateTime.now(), 1)
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(dao)

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockReplayRepository, mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/verify", dto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    val responseBody = Json.decodeFromString(VerifyResponseDto.serializer(), response.content!!)
                    responseBody.status shouldBe ResponseStatus.MATCHED
                    responseBody.agentId shouldBe agentId
                }
            }
        }

        "be able to verify an ANSI-378-2009 template against an existing template" {
            val dtoFilters = VerifyRequestFiltersDto(agentId)
            val dtoParams = VerifyRequestParamsDto(ansi378v2009Template, position)
            val dto = VerifyRequestDto(dtoFilters, dtoParams, DataType.TEMPLATE)
            every { mockReplayRepository.addReplay(any()) } returns ReplayDao(1, "foo", ZonedDateTime.now(), 1)
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(dao)

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockReplayRepository, mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/verify", dto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    val responseBody = Json.decodeFromString(VerifyResponseDto.serializer(), response.content!!)
                    responseBody.status shouldBe ResponseStatus.MATCHED
                    responseBody.agentId shouldBe agentId
                }
            }
        }

        "return an error if the image doesn't verify due to not being an image (improper image format)" {
            val dtoFilters = VerifyRequestFiltersDto(agentId)
            val dtoParams = VerifyRequestParamsDto("foobar", position)
            val dto = VerifyRequestDto(dtoFilters, dtoParams)

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockReplayRepository, mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/verify", dto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.BadRequest
                    response.content shouldNotBe null
                    val responseBody = Json.decodeFromString(ApiError.serializer(), response.content!!)
                    responseBody.code shouldBe BioAuthExceptionCode.InvalidImageFormat.name
                }
            }
        }

        "return an error if the image doesn't verify due to poor image quality" {
            val dtoFilters = VerifyRequestFiltersDto(agentId)
            val dtoParams = VerifyRequestParamsDto(otherImage, position)
            val dto = VerifyRequestDto(dtoFilters, dtoParams)
            every { mockReplayRepository.addReplay(any()) } returns ReplayDao(1, "foo", ZonedDateTime.now(), 1)
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(dao)
            val httpClient = mockHttpClient(BioAnalyzerRoute(bioanalyzerUrl, BioanalyzerReponseDto(1.0)))

            withTestApplication({
                testFingerprintRoutes(appConfig, httpClient, mockReplayRepository, mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/verify", dto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.BadRequest
                    response.content shouldNotBe null
                    val responseBody = Json.decodeFromString(ApiError.serializer(), response.content!!)
                    responseBody.code shouldBe BioAuthExceptionCode.FingerprintLowQuality.name
                }
            }
        }

        "return an error if the image doesn't verify in spite of good image quality" {
            val dtoFilters = VerifyRequestFiltersDto(agentId)
            val dtoParams = VerifyRequestParamsDto(otherImage, position)
            val dto = VerifyRequestDto(dtoFilters, dtoParams)
            every { mockReplayRepository.addReplay(any()) } returns ReplayDao(1, "foo", ZonedDateTime.now(), 1)
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(dao)
            val httpClient = mockHttpClient(BioAnalyzerRoute(bioanalyzerUrl, BioanalyzerReponseDto(99.0)))

            withTestApplication({
                testFingerprintRoutes(appConfig, httpClient, mockReplayRepository, mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/verify", dto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.BadRequest
                    response.content shouldNotBe null
                    val responseBody = Json.decodeFromString(ApiError.serializer(), response.content!!)
                    responseBody.code shouldBe BioAuthExceptionCode.FingerprintNoMatch.name
                }
            }
        }

        "return an error if the stored template has a missing_code" {
            val dtoFilters = VerifyRequestFiltersDto(agentId)
            val dtoParams = VerifyRequestParamsDto(base64Image, position)
            val dto = VerifyRequestDto(dtoFilters, dtoParams)
            every { mockReplayRepository.addReplay(any()) } returns ReplayDao(1, "foo", ZonedDateTime.now(), 1)
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(dao.copy(template = null, missingCode = "XX"))

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockReplayRepository, mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/verify", dto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.BadRequest
                    response.content shouldNotBe null
                    val responseBody = Json.decodeFromString(ApiError.serializer(), response.content!!)
                    responseBody.code shouldBe BioAuthExceptionCode.FingerprintMissingAmputation.name
                }
            }
        }

        "return an error if the stored template was made with a different version" {
            val dtoFilters = VerifyRequestFiltersDto(agentId)
            val dtoParams = VerifyRequestParamsDto(base64Image, position)
            val dto = VerifyRequestDto(dtoFilters, dtoParams)
            every { mockReplayRepository.addReplay(any()) } returns ReplayDao(1, "foo", ZonedDateTime.now(), 1)
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(dao.copy(version = 2))

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockReplayRepository, mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/verify", dto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.BadRequest
                    response.content shouldNotBe null
                    val responseBody = Json.decodeFromString(ApiError.serializer(), response.content!!)
                    responseBody.code shouldBe BioAuthExceptionCode.InvalidTemplateVersion.name
                }
            }
        }
    }
})
