package fingerprint

import com.machinezoo.sourceafis.FingerprintTemplate
import com.typesafe.config.ConfigFactory
import fingerprint.dtos.VerifyResponseDto
import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.ktor.config.HoconApplicationConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.kiva.bioauthservice.app.config.AppConfig
import org.kiva.bioauthservice.bioanalyzer.dtos.BioanalyzerReponseDto
import org.kiva.bioauthservice.common.errors.ApiError
import org.kiva.bioauthservice.common.errors.BioAuthExceptionCode
import org.kiva.bioauthservice.common.utils.base64ToByte
import org.kiva.bioauthservice.common.utils.base64ToString
import org.kiva.bioauthservice.db.daos.FingerprintTemplateDao
import org.kiva.bioauthservice.db.daos.ReplayDao
import org.kiva.bioauthservice.db.repositories.FingerprintTemplateRepository
import org.kiva.bioauthservice.db.repositories.ReplayRepository
import org.kiva.bioauthservice.fingerprint.dtos.VerifyRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.VerifyRequestFiltersDto
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
    val alphanumericStringGen = Arb.string(10, Arb.alphanumeric())
    val did = alphanumericStringGen.next()
    val nationalId = alphanumericStringGen.next()
    val voterId = alphanumericStringGen.next()
    val backend = alphanumericStringGen.next()
    val position = FingerPosition.RIGHT_INDEX
    val template = this.javaClass.getResource("/images/sample_template.txt")?.readText() ?: ""
    val image = this.javaClass.getResource("/images/sample.jpg")?.readBytes()?.base64ToString() ?: ""
    val image2 = this.javaClass.getResource("/images/sample.png")?.readBytes()?.base64ToString() ?: "" // Not the same fingerprint
    val sourceAfisTemplate = FingerprintTemplate(template.base64ToByte())
    val appConfig = AppConfig(HoconApplicationConfig(ConfigFactory.load()))
    val bioanalyzerUrl = appConfig.bioanalyzerConfig.baseUrl + appConfig.bioanalyzerConfig.analyzePath
    val mockReplayRepository = mockk<ReplayRepository>()
    val mockFingerprintTemplateRepository = mockk<FingerprintTemplateRepository>()
    val dao = FingerprintTemplateDao(
        1,
        nationalId,
        voterId,
        did,
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

    "POST /verify" should {

        // TODO: Test to verify it works with foreign templates
        // TODO: Test to verify it works with different image types
        // TODO: Test to verify it fails with invalid image types

        "be able to verify an image against an existing template" {
            val dtoFilters = VerifyRequestFiltersDto(null, null, did)
            val dto = VerifyRequestDto(backend, image, position, dtoFilters)
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
                    responseBody.id shouldBe did
                    responseBody.did shouldBe did
                    responseBody.nationalId shouldBe nationalId
                }
            }
        }

        "be able to verify a template against an existing template" {
            val dtoFilters = VerifyRequestFiltersDto(null, null, did)
            val dto = VerifyRequestDto(backend, template, position, dtoFilters, DataType.TEMPLATE)
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
                    responseBody.id shouldBe did
                    responseBody.did shouldBe did
                    responseBody.nationalId shouldBe nationalId
                }
            }
        }

        "return an error if the images don't verify due to poor image quality" {
            val dtoFilters = VerifyRequestFiltersDto(null, null, did)
            val dto = VerifyRequestDto(backend, image2, position, dtoFilters)
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
                    responseBody.error shouldBe BioAuthExceptionCode.BioanalyzerServerError.name
                }
            }
        }

        "return an error if the images don't verify in spite of good image quality" {
            val dtoFilters = VerifyRequestFiltersDto(null, null, did)
            val dto = VerifyRequestDto(backend, image2, position, dtoFilters)
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
                    responseBody.error shouldBe BioAuthExceptionCode.FingerprintNoMatch.name
                }
            }
        }

        "return an error if the stored template has a missing_code" {
            val dtoFilters = VerifyRequestFiltersDto(null, null, did)
            val dto = VerifyRequestDto(backend, image, position, dtoFilters)
            every { mockReplayRepository.addReplay(any()) } returns ReplayDao(1, "foo", ZonedDateTime.now(), 1)
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(dao.copy(template = null, missingCode = "XX"))

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockReplayRepository, mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/verify", dto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.BadRequest
                    response.content shouldNotBe null
                    val responseBody = Json.decodeFromString(ApiError.serializer(), response.content!!)
                    responseBody.error shouldBe BioAuthExceptionCode.FingerprintMissingAmputation.name
                }
            }
        }

        "return an error if the stored template was made with a different version" {
            val dtoFilters = VerifyRequestFiltersDto(null, null, did)
            val dto = VerifyRequestDto(backend, image, position, dtoFilters)
            every { mockReplayRepository.addReplay(any()) } returns ReplayDao(1, "foo", ZonedDateTime.now(), 1)
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(dao.copy(version = 2))

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockReplayRepository, mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/verify", dto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.BadRequest
                    response.content shouldNotBe null
                    val responseBody = Json.decodeFromString(ApiError.serializer(), response.content!!)
                    responseBody.error shouldBe BioAuthExceptionCode.InvalidTemplateVersion.name
                }
            }
        }
    }
})
