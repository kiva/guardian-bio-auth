package fingerprint

import alphanumericStringGen
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.arbitrary.next
import io.ktor.util.KtorExperimentalAPI
import io.mockk.Called
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.ExperimentalSerializationApi
import org.kiva.bioauthservice.app.config.FingerprintConfig
import org.kiva.bioauthservice.bioanalyzer.BioanalyzerService
import org.kiva.bioauthservice.common.errors.impl.FingerprintTemplateGenerationException
import org.kiva.bioauthservice.common.errors.impl.InvalidImageFormatException
import org.kiva.bioauthservice.common.errors.impl.InvalidTemplateException
import org.kiva.bioauthservice.common.utils.base64ToString
import org.kiva.bioauthservice.db.repositories.FingerprintTemplateRepository
import org.kiva.bioauthservice.fingerprint.FingerprintService
import org.kiva.bioauthservice.fingerprint.dtos.BulkSaveRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestFiltersDto
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestParamsDto
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition
import org.kiva.bioauthservice.replay.ReplayService
import java.time.ZonedDateTime

@ExperimentalSerializationApi
@KtorExperimentalAPI
class FingerprintServiceSpec : WordSpec({

    // Test fixtures
    val did = alphanumericStringGen.next()
    val voterId = alphanumericStringGen.next()
    val nationalId = alphanumericStringGen.next()
    val requestId = alphanumericStringGen.next()
    val template = this.javaClass.getResource("/images/sample_template.txt")?.readText() ?: ""
    val image = this.javaClass.getResource("/images/sample.png")?.readBytes()?.base64ToString() ?: ""
    val mockFingerprintTemplateRepository = mockk<FingerprintTemplateRepository>()
    val mockFingerprintConfig = mockk<FingerprintConfig>()
    val mockReplayService = mockk<ReplayService>()
    val mockBioanalyzerService = mockk<BioanalyzerService>()

    fun buildFingerprintService(): FingerprintService {
        return FingerprintService(
            mockFingerprintTemplateRepository,
            mockFingerprintConfig,
            mockReplayService,
            mockBioanalyzerService
        )
    }

    beforeEach {
        clearAllMocks()
    }

    "save()" should {

        "succeed if provided a missing code" {
            every { mockFingerprintTemplateRepository.insertTemplate(any()) } returns true
            val fpService = buildFingerprintService()
            val dto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        did,
                        SaveRequestFiltersDto(voterId, nationalId),
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, "", "", 0.0, "XX")
                    )
                )
            )
            val result = fpService.save(dto, requestId)
            result shouldBe 1
            coVerify { mockBioanalyzerService wasNot Called }
        }

        "succeed if provided an image" {
            coEvery { mockBioanalyzerService.analyze(any(), any(), any()) } returns 40.0
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true
            val fpService = buildFingerprintService()
            val dto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        did,
                        SaveRequestFiltersDto(voterId, nationalId),
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, image)
                    )
                )
            )
            val result = fpService.save(dto, requestId)
            result shouldBe 1
            coVerify(exactly = 1) { mockBioanalyzerService.analyze(any(), any(), any()) }
        }

        "succeed if provided a template" {
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true
            val fpService = buildFingerprintService()
            val dto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        did,
                        SaveRequestFiltersDto(voterId, nationalId),
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, "", template)
                    )
                )
            )
            val result = fpService.save(dto, requestId)
            result shouldBe 1
            coVerify { mockBioanalyzerService wasNot Called }
        }

        "succeed if provided multiple templates" {
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true
            val fpService = buildFingerprintService()
            val dto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        did,
                        SaveRequestFiltersDto(voterId, nationalId),
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, "", template)
                    ),
                    SaveRequestDto(
                        did,
                        SaveRequestFiltersDto(voterId, nationalId),
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.LEFT_INDEX, "", template)
                    )
                )
            )
            val result = fpService.save(dto, requestId)
            result shouldBe 2
            coVerify { mockBioanalyzerService wasNot Called }
        }

        "succeed if provided multiple images" {
            coEvery { mockBioanalyzerService.analyze(any(), any(), any()) } returns 40.0
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true
            val fpService = buildFingerprintService()
            val dto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        did,
                        SaveRequestFiltersDto(voterId, nationalId),
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, image)
                    ),
                    SaveRequestDto(
                        did,
                        SaveRequestFiltersDto(voterId, nationalId),
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.LEFT_INDEX, image)
                    )
                )
            )
            val result = fpService.save(dto, requestId)
            result shouldBe 2
            coVerify(exactly = 2) { mockBioanalyzerService.analyze(any(), any(), any()) }
        }

        "succeed if provided a mix of images and templates" {
            coEvery { mockBioanalyzerService.analyze(any(), any(), any()) } returns 40.0
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true
            val fpService = buildFingerprintService()
            val dto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        did,
                        SaveRequestFiltersDto(voterId, nationalId),
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, image)
                    ),
                    SaveRequestDto(
                        did,
                        SaveRequestFiltersDto(voterId, nationalId),
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.LEFT_INDEX, "", template)
                    )
                )
            )
            val result = fpService.save(dto, requestId)
            result shouldBe 2
            coVerify(exactly = 1) { mockBioanalyzerService.analyze(any(), any(), any()) }
        }

        "fail if provided a single entry with both an image and a missing_code" {
            val fpService = buildFingerprintService()
            val dto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        did,
                        SaveRequestFiltersDto(voterId, nationalId),
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, image, "", 0.0, "XX")
                    )
                )
            )
            shouldThrow<FingerprintTemplateGenerationException> {
                fpService.save(dto, requestId)
            }
            coVerify { mockBioanalyzerService wasNot Called }
        }

        "fail if provided a single entry with both a template and a missing_code" {
            val fpService = buildFingerprintService()
            val dto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        did,
                        SaveRequestFiltersDto(voterId, nationalId),
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, "", template, 0.0, "XX")
                    )
                )
            )
            shouldThrow<FingerprintTemplateGenerationException> {
                fpService.save(dto, requestId)
            }
            coVerify { mockBioanalyzerService wasNot Called }
        }

        "fail to save an base64-encoded image with an unsupported format" {
            val badImage = "foobar".toByteArray().base64ToString()
            val fpService = buildFingerprintService()
            val dto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        did,
                        SaveRequestFiltersDto(voterId, nationalId),
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, badImage)
                    )
                )
            )
            shouldThrow<InvalidImageFormatException> {
                fpService.save(dto, requestId)
            }
            coVerify { mockBioanalyzerService wasNot Called }
        }

        "fail to save a fingerprint template that is not actually a fingerprint template" {
            val badTemplate = "foobar".toByteArray().base64ToString()
            val fpService = buildFingerprintService()
            val dto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        did,
                        SaveRequestFiltersDto(voterId, nationalId),
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, "", badTemplate)
                    )
                )
            )
            shouldThrow<InvalidTemplateException> {
                fpService.save(dto, requestId)
            }
            coVerify { mockBioanalyzerService wasNot Called }
        }
    }
})
