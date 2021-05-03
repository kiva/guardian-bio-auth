package org.kiva.bioauthservice.fingerprint

import com.machinezoo.sourceafis.FingerprintCompatibility
import com.machinezoo.sourceafis.FingerprintImage
import com.machinezoo.sourceafis.FingerprintMatcher
import com.machinezoo.sourceafis.FingerprintTemplate
import fingerprint.dtos.VerifyResponseDto
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.jnbis.api.Jnbis
import org.kiva.bioauthservice.common.errors.impl.FingerprintMissingAmputationException
import org.kiva.bioauthservice.common.errors.impl.FingerprintMissingNotCapturedException
import org.kiva.bioauthservice.common.errors.impl.FingerprintMissingUnableToPrintException
import org.kiva.bioauthservice.common.errors.impl.FingerprintTemplateGenerationException
import org.kiva.bioauthservice.common.errors.impl.InvalidImageFormatException
import org.kiva.bioauthservice.common.utils.detectContentType
import org.kiva.bioauthservice.db.repositories.FingerprintTemplateRepository
import org.kiva.bioauthservice.fingerprint.dtos.BulkSaveRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.VerifyRequestDto
import org.kiva.bioauthservice.fingerprint.enums.DataType
import org.kiva.bioauthservice.fingerprint.enums.ResponseStatus
import org.kiva.bioauthservice.replay.ReplayService
import java.io.ByteArrayInputStream
import java.io.DataInputStream

@KtorExperimentalAPI
class FingerprintService(
    private val templateRepository: FingerprintTemplateRepository,
    private val fingerprintConfig: FingerprintConfig,
    private val replayService: ReplayService
) {

    private fun isForeignTemplate(template: ByteArray): Boolean {
        val input = DataInputStream(ByteArrayInputStream(template))
        return input.readByte() == 'F'.toByte() && input.readByte() == 'M'.toByte() && input.readByte() == 'R'.toByte()
    }

    private fun buildTemplate(template: ByteArray): FingerprintTemplateWrapper {
        if (isForeignTemplate(template)) {
            return FingerprintTemplateWrapper(FingerprintCompatibility.importTemplate(template))
        } else {
            return FingerprintTemplateWrapper(FingerprintTemplate(template))
        }
    }

    private fun buildTemplateFromImage(image: ByteArray): FingerprintTemplateWrapper {
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
        return FingerprintTemplateWrapper(FingerprintTemplate(fpImg))
    }

    @ExperimentalSerializationApi
    fun save(bulkDto: BulkSaveRequestDto): Int {

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
                val template = FingerprintTemplateWrapper(null)
                templateRepository.insertTemplate(template, dto)
            } else if (dto.params.type == DataType.TEMPLATE) {
                val template = buildTemplate(dto.params.fingerprintBytes)
                templateRepository.insertTemplate(template, dto)
            } else {
                // TODO Calculate score
                val score = 0.0
                val template = buildTemplateFromImage(dto.params.fingerprintBytes)
                templateRepository.insertTemplate(template, dto, score)
            }
        }
    }

    fun verify(dto: VerifyRequestDto): VerifyResponseDto {

        // Check for replay attacks
        replayService.checkIfReplay(dto.params.imageByte)

        // Get template (including check for image type in case the image is provided in an unsupported format)
        val targetTemplate = if (dto.imageType == DataType.TEMPLATE) {
            buildTemplate(dto.params.imageByte)
        } else {
            buildTemplateFromImage(dto.params.imageByte)
        }

        // Find all candidates that do not have a missing code and have a match score >= matchThreshold
        val matcher = FingerprintMatcher(targetTemplate.fingerprintTemplate)
        val matches = templateRepository
            .getTemplates(dto.filters, dto.params.position)
            .map {
                if (!it.missingCode.isNullOrBlank() || it.template == null) {
                    when (it.missingCode) {
                        "NA" -> throw FingerprintMissingNotCapturedException()
                        "XX" -> throw FingerprintMissingAmputationException()
                        "UP" -> throw FingerprintMissingUnableToPrintException()
                        else -> throw FingerprintMissingNotCapturedException() // default to NA if not available.
                    }
                }
                val matchingScore = matcher.match(it.template)
                VerifyResponseDto(ResponseStatus.MATCHED, it.did, it.did, it.nationalId, matchingScore)
            }
            .filter { it.matchingScore!! >= fingerprintConfig.matchThreshold }
            .sortedBy { it.matchingScore }

        // Done, return the result
        return if (matches.isEmpty()) {
            // TODO: If no matches, query bioanalyzer service to determine if the image provided is low quality
            VerifyResponseDto(ResponseStatus.NOT_MATCHED)
        } else {
            matches.last()
        }
    }
}
