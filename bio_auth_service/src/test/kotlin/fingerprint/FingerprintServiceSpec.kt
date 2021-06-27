package fingerprint

import alphanumericStringGen
import com.machinezoo.sourceafis.FingerprintTemplate
import common.errors.impl.FingerprintNoMatchException
import common.errors.impl.InvalidFilterException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.arbitrary.next
import io.ktor.util.KtorExperimentalAPI
import io.mockk.Called
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.serialization.ExperimentalSerializationApi
import org.kiva.bioauthservice.app.config.FingerprintConfig
import org.kiva.bioauthservice.bioanalyzer.BioanalyzerService
import org.kiva.bioauthservice.common.errors.impl.FingerprintMissingAmputationException
import org.kiva.bioauthservice.common.errors.impl.FingerprintMissingNotCapturedException
import org.kiva.bioauthservice.common.errors.impl.FingerprintMissingUnableToPrintException
import org.kiva.bioauthservice.common.errors.impl.FingerprintTemplateGenerationException
import org.kiva.bioauthservice.common.errors.impl.InvalidImageFormatException
import org.kiva.bioauthservice.common.errors.impl.InvalidParamsException
import org.kiva.bioauthservice.common.errors.impl.InvalidTemplateException
import org.kiva.bioauthservice.common.errors.impl.InvalidTemplateVersionException
import org.kiva.bioauthservice.common.utils.base64ToByte
import org.kiva.bioauthservice.common.utils.toBase64String
import org.kiva.bioauthservice.common.utils.toHexString
import org.kiva.bioauthservice.db.daos.FingerprintTemplateDao
import org.kiva.bioauthservice.db.repositories.FingerprintTemplateRepository
import org.kiva.bioauthservice.fingerprint.FingerprintService
import org.kiva.bioauthservice.fingerprint.dtos.BulkSaveRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestParamsDto
import org.kiva.bioauthservice.fingerprint.dtos.VerifyRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.VerifyRequestFiltersDto
import org.kiva.bioauthservice.fingerprint.enums.DataType
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition
import org.kiva.bioauthservice.fingerprint.enums.ResponseStatus
import org.kiva.bioauthservice.replay.ReplayService
import java.time.ZonedDateTime

@ExperimentalSerializationApi
@KtorExperimentalAPI
class FingerprintServiceSpec : WordSpec({

    // Test fixtures
    val agentId = alphanumericStringGen.next()
    val requestId = alphanumericStringGen.next()
    val backend = alphanumericStringGen.next()
    val position = FingerPosition.RIGHT_INDEX
    val sourceAfisTemplate = this.javaClass.getResource("/images/sample_source_afis_template.txt")?.readText() ?: ""
    val ansi378v2004Template = this.javaClass.getResource("/images/sample_ansi_378_2004_template.txt")?.readText() ?: ""
    val ansi378v2009Template = this.javaClass.getResource("/images/sample_ansi_378_2009_template.txt")?.readText() ?: ""
    val base64Image = this.javaClass.getResource("/images/sample.jpg")?.readBytes()?.toBase64String() ?: ""
    val hexImage = this.javaClass.getResource("/images/sample.jpg")?.readBytes()?.toHexString() ?: ""
    val wsqImage = this.javaClass.getResource("/images/sample.wsq")?.readBytes()?.toBase64String() ?: ""
    val wrongImage = this.javaClass.getResource("/images/sample.png")?.readBytes()?.toBase64String() ?: ""
    val dao = FingerprintTemplateDao(
        1,
        agentId,
        1,
        3,
        position,
        "sourceafis",
        FingerprintTemplate(sourceAfisTemplate.base64ToByte()),
        null,
        99,
        ZonedDateTime.now(),
        ZonedDateTime.now(),
        ZonedDateTime.now()
    )
    val verifyRequestDto = VerifyRequestDto(
        backend,
        sourceAfisTemplate,
        position,
        VerifyRequestFiltersDto(
            agentId
        ),
        DataType.TEMPLATE
    )

    // Mocks
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
                        agentId,
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, "", "", 0.0, "XX")
                    )
                )
            )
            val result = fpService.save(dto, requestId)
            result shouldBe 1
            coVerify { mockBioanalyzerService wasNot Called }
        }

        "succeed if provided a base64 image" {
            coEvery { mockBioanalyzerService.analyze(any(), any(), any()) } returns 40.0
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true
            val fpService = buildFingerprintService()
            val dto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        agentId,
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, base64Image)
                    )
                )
            )
            val result = fpService.save(dto, requestId)
            result shouldBe 1
            coVerify(exactly = 1) { mockBioanalyzerService.analyze(any(), any(), any()) }
        }

        "succeed if provided a hex image" {
            coEvery { mockBioanalyzerService.analyze(any(), any(), any()) } returns 40.0
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true
            val fpService = buildFingerprintService()
            val dto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        agentId,
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, hexImage)
                    )
                )
            )
            val result = fpService.save(dto, requestId)
            result shouldBe 1
            coVerify(exactly = 1) { mockBioanalyzerService.analyze(any(), any(), any()) }
        }

        "succeed if provided a wsq image" {
            coEvery { mockBioanalyzerService.analyze(any(), any(), any()) } returns 40.0
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true
            val fpService = buildFingerprintService()
            val dto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        agentId,
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, wsqImage)
                    )
                )
            )
            val result = fpService.save(dto, requestId)
            result shouldBe 1
            coVerify(exactly = 1) { mockBioanalyzerService.analyze(any(), any(), any()) }
        }

        "succeed if provided a SourceAFIS template" {
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true
            val fpService = buildFingerprintService()
            val dto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        agentId,
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, "", sourceAfisTemplate)
                    )
                )
            )
            val result = fpService.save(dto, requestId)
            result shouldBe 1
            coVerify { mockBioanalyzerService wasNot Called }
        }

        "succeed if provided an ANSI 378-2004 template" {
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true
            val fpService = buildFingerprintService()
            val dto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        agentId,
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, "", ansi378v2004Template)
                    )
                )
            )
            val result = fpService.save(dto, requestId)
            result shouldBe 1
            coVerify { mockBioanalyzerService wasNot Called }
        }

        "succeed if provided an ANSI 378-2009 template" {
            every { mockFingerprintTemplateRepository.insertTemplate(any(), any(), any()) } returns true
            val fpService = buildFingerprintService()
            val dto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        agentId,
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, "", ansi378v2009Template)
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
                        agentId,
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, "", sourceAfisTemplate)
                    ),
                    SaveRequestDto(
                        agentId,
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.LEFT_INDEX, "", sourceAfisTemplate)
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
                        agentId,
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, base64Image)
                    ),
                    SaveRequestDto(
                        agentId,
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.LEFT_INDEX, base64Image)
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
                        agentId,
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, base64Image)
                    ),
                    SaveRequestDto(
                        agentId,
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.LEFT_INDEX, "", sourceAfisTemplate)
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
                        agentId,
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, base64Image, "", 0.0, "XX")
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
                        agentId,
                        SaveRequestParamsDto(1, ZonedDateTime.now(), FingerPosition.RIGHT_INDEX, "", sourceAfisTemplate, 0.0, "XX")
                    )
                )
            )
            shouldThrow<FingerprintTemplateGenerationException> {
                fpService.save(dto, requestId)
            }
            coVerify { mockBioanalyzerService wasNot Called }
        }

        "fail to save an base64-encoded image with an unsupported format" {
            val badImage = "foobar".toByteArray().toBase64String()
            val fpService = buildFingerprintService()
            val dto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        agentId,
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
            val badTemplate = "foobar".toByteArray().toBase64String()
            val fpService = buildFingerprintService()
            val dto = BulkSaveRequestDto(
                listOf(
                    SaveRequestDto(
                        agentId,
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

    "verify()" should {

        "be able to match a Source AFIS template against a stored template" {
            every { mockFingerprintConfig.maxTargets } returns 2
            every { mockFingerprintConfig.matchThreshold } returns 40.0
            every { mockReplayService.checkIfReplay(any()) } just Runs
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(dao)
            val fpService = buildFingerprintService()
            val result = fpService.verify(verifyRequestDto, requestId)
            result.status shouldBe ResponseStatus.MATCHED
            result.agentId shouldBe agentId
            result.matchingScore shouldNotBe null
            result.matchingScore!! shouldBeGreaterThan 0.0
        }

        "be able to match an ANSI 378-2004 template against a stored template" {
            every { mockFingerprintConfig.maxTargets } returns 2
            every { mockFingerprintConfig.matchThreshold } returns 40.0
            every { mockReplayService.checkIfReplay(any()) } just Runs
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(dao)
            val dto = verifyRequestDto.copy(image = ansi378v2004Template)
            val fpService = buildFingerprintService()
            val result = fpService.verify(dto, requestId)
            result.status shouldBe ResponseStatus.MATCHED
            result.agentId shouldBe agentId
            result.matchingScore shouldNotBe null
            result.matchingScore!! shouldBeGreaterThan 0.0
        }

        "be able to match an ANSI 378-2009 template against a stored template" {
            every { mockFingerprintConfig.maxTargets } returns 2
            every { mockFingerprintConfig.matchThreshold } returns 40.0
            every { mockReplayService.checkIfReplay(any()) } just Runs
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(dao)
            val dto = verifyRequestDto.copy(image = ansi378v2009Template)
            val fpService = buildFingerprintService()
            val result = fpService.verify(dto, requestId)
            result.status shouldBe ResponseStatus.MATCHED
            result.agentId shouldBe agentId
            result.matchingScore shouldNotBe null
            result.matchingScore!! shouldBeGreaterThan 0.0
        }

        "be able to match a base64 image against a stored template" {
            every { mockFingerprintConfig.maxTargets } returns 2
            every { mockFingerprintConfig.matchThreshold } returns 40.0
            every { mockReplayService.checkIfReplay(any()) } just Runs
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(dao)
            val dto = verifyRequestDto.copy(image = base64Image, imageType = DataType.IMAGE)
            val fpService = buildFingerprintService()
            val result = fpService.verify(dto, requestId)
            result.status shouldBe ResponseStatus.MATCHED
            result.agentId shouldBe agentId
            result.matchingScore shouldNotBe null
            result.matchingScore!! shouldBeGreaterThan 0.0
        }

        "be able to match a hex image against a stored template" {
            every { mockFingerprintConfig.maxTargets } returns 2
            every { mockFingerprintConfig.matchThreshold } returns 40.0
            every { mockReplayService.checkIfReplay(any()) } just Runs
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(dao)
            val dto = verifyRequestDto.copy(image = hexImage, imageType = DataType.IMAGE)
            val fpService = buildFingerprintService()
            val result = fpService.verify(dto, requestId)
            result.status shouldBe ResponseStatus.MATCHED
            result.agentId shouldBe agentId
            result.matchingScore shouldNotBe null
            result.matchingScore!! shouldBeGreaterThan 0.0
        }

        "fail if not provided an image or template to match against" {
            every { mockFingerprintConfig.maxTargets } returns 2
            val fpService = buildFingerprintService()
            val dto = verifyRequestDto.copy(image = "")
            shouldThrow<InvalidParamsException> {
                fpService.verify(dto, requestId)
            }
        }

        "fail if too many AgentIds are provided to match against" {
            every { mockFingerprintConfig.maxTargets } returns 2
            val fpService = buildFingerprintService()
            val agentIds = "$agentId,${alphanumericStringGen.next()},${alphanumericStringGen.next()}"
            val dto = verifyRequestDto.copy(filters = VerifyRequestFiltersDto(agentIds))
            shouldThrow<InvalidFilterException> {
                fpService.verify(dto, requestId)
            }
        }

        "fail if provided an improperly formatted template" {
            val badTemplate = "foobar".toByteArray().toBase64String()
            every { mockFingerprintConfig.maxTargets } returns 2
            every { mockFingerprintConfig.matchThreshold } returns 40.0
            every { mockReplayService.checkIfReplay(any()) } just Runs
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(dao)
            val fpService = buildFingerprintService()
            val dto = verifyRequestDto.copy(image = badTemplate)
            shouldThrow<InvalidTemplateException> {
                fpService.verify(dto, requestId)
            }
        }

        "fail if provided an improperly formatted image" {
            val badImage = "foobar".toByteArray().toBase64String()
            every { mockFingerprintConfig.maxTargets } returns 2
            every { mockFingerprintConfig.matchThreshold } returns 40.0
            every { mockReplayService.checkIfReplay(any()) } just Runs
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(dao)
            val fpService = buildFingerprintService()
            val dto = verifyRequestDto.copy(image = badImage, imageType = DataType.IMAGE)
            shouldThrow<InvalidImageFormatException> {
                fpService.verify(dto, requestId)
            }
        }

        "fail to match a high-quality image that doesn't correspond to the stored template" {
            every { mockFingerprintConfig.maxTargets } returns 2
            every { mockFingerprintConfig.matchThreshold } returns 40.0
            every { mockReplayService.checkIfReplay(any()) } just Runs
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(dao)
            coEvery { mockBioanalyzerService.analyze(any(), any(), any()) } returns 99.9
            val dto = verifyRequestDto.copy(image = wrongImage, imageType = DataType.IMAGE)
            val fpService = buildFingerprintService()
            shouldThrow<FingerprintNoMatchException> {
                fpService.verify(dto, requestId)
            }
        }

        "fail with an Amputation exception if the stored template has an XX missing code" {
            every { mockFingerprintConfig.maxTargets } returns 2
            every { mockReplayService.checkIfReplay(any()) } just Runs
            val missingCodeDao = dao.copy(template = null, missingCode = "XX")
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(missingCodeDao)
            val fpService = buildFingerprintService()
            shouldThrow<FingerprintMissingAmputationException> {
                fpService.verify(verifyRequestDto, requestId)
            }
        }

        "fail with a NotCaptured exception if the stored template has an NA missing code" {
            every { mockFingerprintConfig.maxTargets } returns 2
            every { mockReplayService.checkIfReplay(any()) } just Runs
            val missingCodeDao = dao.copy(template = null, missingCode = "NA")
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(missingCodeDao)
            val fpService = buildFingerprintService()
            shouldThrow<FingerprintMissingNotCapturedException> {
                fpService.verify(verifyRequestDto, requestId)
            }
        }

        "fail with an UnableToPrint exception if the stored template has a UP missing code" {
            every { mockFingerprintConfig.maxTargets } returns 2
            every { mockReplayService.checkIfReplay(any()) } just Runs
            val missingCodeDao = dao.copy(template = null, missingCode = "UP")
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(missingCodeDao)
            val fpService = buildFingerprintService()
            shouldThrow<FingerprintMissingUnableToPrintException> {
                fpService.verify(verifyRequestDto, requestId)
            }
        }

        "fail with a NotCaptured exception if the stored template has an unexpected missing code" {
            every { mockFingerprintConfig.maxTargets } returns 2
            every { mockReplayService.checkIfReplay(any()) } just Runs
            val missingCodeDao = dao.copy(template = null, missingCode = "AA")
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(missingCodeDao)
            val fpService = buildFingerprintService()
            shouldThrow<FingerprintMissingNotCapturedException> {
                fpService.verify(verifyRequestDto, requestId)
            }
        }

        "fail with an InvalidTemplateVersion exception if the stored template comes from a different SourceAFIS version" {
            every { mockFingerprintConfig.maxTargets } returns 2
            every { mockReplayService.checkIfReplay(any()) } just Runs
            val missingCodeDao = dao.copy(version = 2)
            every { mockFingerprintTemplateRepository.getTemplates(any(), any()) } returns listOf(missingCodeDao)
            val fpService = buildFingerprintService()
            shouldThrow<InvalidTemplateVersionException> {
                fpService.verify(verifyRequestDto, requestId)
            }
        }
    }
})
