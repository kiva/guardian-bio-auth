package org.kiva.bioauthservice.fingerprint

import com.machinezoo.sourceafis.FingerprintCompatibility
import com.machinezoo.sourceafis.FingerprintImage
import com.machinezoo.sourceafis.FingerprintMatcher
import com.machinezoo.sourceafis.FingerprintTemplate
import common.errors.impl.FingerprintNoMatchException
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
import org.kiva.bioauthservice.common.errors.impl.InvalidTemplateException
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
        try {
            if (isForeignTemplate(template)) {
                return FingerprintCompatibility.importTemplate(template)
            } else {
                return FingerprintTemplate(template)
            }
        } catch (ex: Exception) {
            throw InvalidTemplateException("Unsupported fingerprint template format for provided fingerprint template")
        }
    }

    private fun buildTemplateFromImage(image: ByteArray): FingerprintTemplate {
        val contentType = image.detectContentType()
        try {
            val bytes = if (contentType === "application/octet-stream") {
                Jnbis.wsq().decode(image).toJpg().asByteArray()
            } else {
                image
            }
            return FingerprintTemplate(FingerprintImage(bytes))
        } catch (ex: Exception) {
            throw InvalidImageFormatException("Unsupported image format for provided fingerprint image: $contentType")
        }
    }

    @ExperimentalSerializationApi
    suspend fun save(bulkDto: BulkSaveRequestDto, requestId: String): Int {

        // Verify a missing code is not provided at the same time that an image or template is provided. These are mutually exclusive.
        bulkDto.fingerprints.forEach { dto: SaveRequestDto ->
            if ((!dto.params.image.isNullOrBlank() || !dto.params.template.isNullOrBlank()) && !dto.params.missing_code.isNullOrBlank()) {
                throw FingerprintTemplateGenerationException(
                    dto.agentId,
                    dto.params.position,
                    "Only one of fingerprint image, template, or missing code should be present for each fingerprint."
                )
            }
        }

        // 3 cases: fingerprint image/template is missing, fingerprint template is provided, fingerprint image is provided
        // Does not throw exception if score is low so we can still save images
        return bulkDto.fingerprints.count { dto: SaveRequestDto ->
            if (!dto.params.missing_code.isNullOrBlank()) {
                templateRepository.insertTemplate(dto)
            } else if (dto.params.type == DataType.TEMPLATE) {
                val template = buildTemplate(dto.params.fingerprintBytes)
                templateRepository.insertTemplate(dto, template)
            } else {
                // If exception thrown here while processing the image, we will not send the cross-service request to Bioanalyzer Service
                val template = buildTemplateFromImage(dto.params.fingerprintBytes)
                val score = bioanalyzerService.analyze(dto.params.image ?: "", false, requestId)
                templateRepository.insertTemplate(dto, template, score)
            }
        }
    }

    suspend fun verify(dto: VerifyRequestDto, requestId: String): VerifyResponseDto {

        // Verify request parameters
        val agentIds = dto.filters.agentIds.split(',')
        if (agentIds.size > fingerprintConfig.maxTargets) {
            throw InvalidFilterException("Too many Agent Ids to match against; the maximum number is ${fingerprintConfig.maxTargets}")
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
            .getTemplates(agentIds, dto.params.position)
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
                VerifyResponseDto(ResponseStatus.MATCHED, it.agentId, matchingScore)
            }
            .filter { it.matchingScore!! >= fingerprintConfig.matchThreshold }
            .sortedBy { it.matchingScore }

        // Done, return the result
        return if (matches.isEmpty()) {
            bioanalyzerService.analyze(dto.params.image, true, requestId)
            throw FingerprintNoMatchException()
        } else {
            matches.last()
        }
    }

    fun positions(positionsDto: PositionsDto): List<FingerPosition> {
        val agentIds = positionsDto.agentIds.split(',')
        return templateRepository.getPositions(agentIds)
    }
}
