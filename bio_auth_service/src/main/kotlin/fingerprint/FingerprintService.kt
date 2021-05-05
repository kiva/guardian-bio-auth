package org.kiva.bioauthservice.fingerprint

import com.machinezoo.sourceafis.FingerprintCompatibility
import com.machinezoo.sourceafis.FingerprintImage
import com.machinezoo.sourceafis.FingerprintMatcher
import com.machinezoo.sourceafis.FingerprintTemplate
import common.errors.impl.InvalidFilterException
import fingerprint.dtos.VerifyResponseDto
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.jnbis.api.Jnbis
import org.kiva.bioauthservice.app.config.FingerprintConfig
import org.kiva.bioauthservice.bioanalyzer.BioanalyzerService
import org.kiva.bioauthservice.common.errors.impl.FingerprintMissingAmputationException
import org.kiva.bioauthservice.common.errors.impl.FingerprintMissingNotCapturedException
import org.kiva.bioauthservice.common.errors.impl.FingerprintMissingUnableToPrintException
import org.kiva.bioauthservice.common.errors.impl.FingerprintTemplateGenerationException
import org.kiva.bioauthservice.common.errors.impl.InvalidImageFormatException
import org.kiva.bioauthservice.common.errors.impl.InvalidParamsException
import org.kiva.bioauthservice.common.errors.impl.InvalidTemplateVersionException
import org.kiva.bioauthservice.common.utils.detectContentType
import org.kiva.bioauthservice.db.repositories.FingerprintTemplateRepository
import org.kiva.bioauthservice.fingerprint.dtos.BulkSaveRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.PositionsDto
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.VerifyRequestDto
import org.kiva.bioauthservice.fingerprint.enums.DataType
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition
import org.kiva.bioauthservice.fingerprint.enums.ResponseStatus
import org.kiva.bioauthservice.replay.ReplayService
import java.io.ByteArrayInputStream
import java.io.DataInputStream

@KtorExperimentalAPI
class FingerprintService(
    private val templateRepository: FingerprintTemplateRepository,
    private val fingerprintConfig: FingerprintConfig,
    private val replayService: ReplayService,
    private val bioanalyzerService: BioanalyzerService
) {

    private fun isForeignTemplate(template: ByteArray): Boolean {
        val input = DataInputStream(ByteArrayInputStream(template))
        return input.readByte() == 'F'.toByte() && input.readByte() == 'M'.toByte() && input.readByte() == 'R'.toByte()
    }

    private fun buildTemplate(template: ByteArray): FingerprintTemplate {
        if (isForeignTemplate(template)) {
            return FingerprintCompatibility.importTemplate(template)
        } else {
            return FingerprintTemplate(template)
        }
    }

    private fun buildTemplateFromImage(image: ByteArray): FingerprintTemplate {
        val contentType = image.detectContentType()
        val fpImg = if (contentType === "application/octet-stream") {
            try {
                val asWsg = Jnbis.wsq().decode(image)
                FingerprintImage(asWsg.asBitmap().pixels)
            } catch (ex: Exception) {
                throw InvalidImageFormatException("Unsupported image format for provided fingerprint image: application/octet-stream")
            }
        } else {
            FingerprintImage(image)
        }
        return FingerprintTemplate(fpImg)
    }

    @ExperimentalSerializationApi
    suspend fun save(bulkDto: BulkSaveRequestDto, requestId: String): Int {

        // Verify a missing code is not provided at the same time that an image or template is provided. These are mutually exclusive.
        bulkDto.fingerprints.forEach { dto: SaveRequestDto ->
            if ((dto.params.image.isNotBlank() || dto.params.template.isNotBlank()) && !dto.params.missing_code.isNullOrBlank()) {
                throw FingerprintTemplateGenerationException(
                    dto.id,
                    dto.params.position,
                    "Either fingerprint image or missing code should be present for each fingerprint."
                )
            }
        }

        // 3 cases: fingerprint image/template is missing, fingerprint template is provided, fingerprint image is provided
        return bulkDto.fingerprints.count { dto: SaveRequestDto ->
            if (!dto.params.missing_code.isNullOrBlank()) {
                templateRepository.insertTemplate(dto)
            } else if (dto.params.type == DataType.TEMPLATE) {
                val template = buildTemplate(dto.params.fingerprintBytes)
                templateRepository.insertTemplate(dto, template)
            } else {
                // Does not throw exception if score is low so we can still save images
                val score = bioanalyzerService.analyze(dto.params.image, true, requestId)
                val template = buildTemplateFromImage(dto.params.fingerprintBytes)
                templateRepository.insertTemplate(dto, template, score)
            }
        }
    }

    suspend fun verify(dto: VerifyRequestDto, requestId: String): VerifyResponseDto {

        // Verify request parameters
        val dids = dto.filters.dids?.split(",") ?: emptyList()
        if (dids.size > fingerprintConfig.maxDids) {
            throw InvalidFilterException("Too many DIDs to match against; the maximum number of DIDs is ${fingerprintConfig.maxDids}")
        }
        if (dto.params.image.isBlank()) {
            throw InvalidParamsException("Image must be a non-empty string")
        }

        // Check for replay attacks
        replayService.checkIfReplay(dto.params.imageByte)

        // Get template (including check for image type in case the image is provided in an unsupported format)
        val targetTemplate = if (dto.imageType == DataType.TEMPLATE) {
            buildTemplate(dto.params.imageByte)
        } else {
            buildTemplateFromImage(dto.params.imageByte)
        }

        // Find all candidates that do not have a missing code and have a match score >= matchThreshold
        val matcher = FingerprintMatcher(targetTemplate)
        val matches = templateRepository
            .getTemplates(dto.filters, dto.params.position)
            .map {
                // Throw if template is empty
                if (!it.missingCode.isNullOrBlank() || it.template == null) {
                    when (it.missingCode) {
                        "NA" -> throw FingerprintMissingNotCapturedException()
                        "XX" -> throw FingerprintMissingAmputationException()
                        "UP" -> throw FingerprintMissingUnableToPrintException()
                        else -> throw FingerprintMissingNotCapturedException() // default to NA if not available.
                    }
                }
                // Throw if version doesn't match
                if (it.version != 3) {
                    throw InvalidTemplateVersionException("Template version does not match the version of the stored template")
                }
                val matchingScore = matcher.match(it.template)
                VerifyResponseDto(ResponseStatus.MATCHED, it.did, it.did, it.nationalId, matchingScore)
            }
            .filter { it.matchingScore!! >= fingerprintConfig.matchThreshold }
            .sortedBy { it.matchingScore }

        // Done, return the result
        return if (matches.isEmpty()) {
            bioanalyzerService.analyze(dto.params.image, true, requestId)
            VerifyResponseDto(ResponseStatus.NOT_MATCHED)
        } else {
            matches.last()
        }
    }

    fun positions(positionsDto: PositionsDto): List<FingerPosition> {
        return templateRepository.getPositions(positionsDto)
    }
}
