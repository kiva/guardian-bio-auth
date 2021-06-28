package fingerprint.routes

import alphanumericStringGen
import com.typesafe.config.ConfigFactory
import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
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
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.kiva.bioauthservice.app.config.AppConfig
import org.kiva.bioauthservice.db.repositories.FingerprintTemplateRepository
import org.kiva.bioauthservice.fingerprint.dtos.PositionsDto
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition

fun PositionsDto.serialize(): String {
    return Json.encodeToString(PositionsDto.serializer(), this)
}

@ExperimentalSerializationApi
@KtorExperimentalAPI
class FingerprintPositionsRoutesSpec : WordSpec({

    // Test fixtures
    val mockFingerprintTemplateRepository = mockk<FingerprintTemplateRepository>()
    val appConfig = AppConfig(HoconApplicationConfig(ConfigFactory.load()))

    beforeEach {
        clearAllMocks()
    }

    "POST /positions" should {

        "return a single position if the agentId matches just one template" {
            val positions = listOf(FingerPosition.RIGHT_INDEX)
            every { mockFingerprintTemplateRepository.getPositions(any()) } returns positions
            val dto = PositionsDto(alphanumericStringGen.next())

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockk(), mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/positions", dto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    val responseBody = Json.decodeFromString(ListSerializer(Int.Companion.serializer()), response.content!!)
                    responseBody shouldHaveSize positions.size
                    responseBody shouldContain FingerPosition.RIGHT_INDEX.code
                }
            }
        }

        "return multiple positions if the agentId matches multiple templates" {
            val positions = listOf(FingerPosition.RIGHT_INDEX, FingerPosition.RIGHT_MIDDLE)
            every { mockFingerprintTemplateRepository.getPositions(any()) } returns positions
            val dto = PositionsDto(alphanumericStringGen.next())

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockk(), mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/positions", dto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    val responseBody = Json.decodeFromString(ListSerializer(Int.Companion.serializer()), response.content!!)
                    responseBody shouldHaveSize positions.size
                    positions.forEach { responseBody shouldContain it.code }
                }
            }
        }

        "return an empty array if the agentId matches no template" {
            every { mockFingerprintTemplateRepository.getPositions(any()) } returns emptyList()
            val dto = PositionsDto(alphanumericStringGen.next())

            withTestApplication({
                testFingerprintRoutes(appConfig, mockk(), mockk(), mockFingerprintTemplateRepository)
            }) {
                post("/api/v1/positions", dto.serialize()) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null
                    val responseBody = Json.decodeFromString(ListSerializer(Int.Companion.serializer()), response.content!!)
                    responseBody shouldHaveSize 0
                }
            }
        }
    }
})
