package org.kiva.bioauthservice.fingerprint

import com.machinezoo.sourceafis.FingerprintCompatibility
import com.machinezoo.sourceafis.FingerprintImage
import com.machinezoo.sourceafis.FingerprintTemplate
import fingerprint.dtos.VerifyResponseDto
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.jnbis.api.Jnbis
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

        bulkDto.fingerprints.forEach { dto: SaveRequestDto ->
            if ((dto.params.image.isNotBlank() || dto.params.template.isNotBlank()) && !dto.params.missing_code.isNullOrBlank()) {
                throw FingerprintTemplateGenerationException(
                    dto.id,
                    dto.params.position,
                    "Either fingerprint image or missing code should be present for each fingerprint."
                )
            }
        }

        // TODO: Handle intersection between missing, templates, saved (so nothing gets saved twice)
        val numMissingSaved = bulkDto.fingerprints
            .filter { dto: SaveRequestDto -> !dto.params.missing_code.isNullOrBlank() }
            .count { dto: SaveRequestDto ->
                val template = FingerprintTemplateWrapper(null)
                templateRepository.insertTemplate(template, dto)
            }

        // Handle templates
        val numSavedTemplates = bulkDto.fingerprints
            .filter { it.params.type == DataType.TEMPLATE }
            .count { dto: SaveRequestDto ->
                val template = buildTemplate(dto.params.fingerprintBytes)
                templateRepository.insertTemplate(template, dto)
            }

        // Handle images
        val numSavedImages = bulkDto.fingerprints
            .filter { it.params.type == DataType.IMAGE }
            .count { dto: SaveRequestDto ->
                // TODO Calculate score
                val score = 0.0
                val template = buildTemplateFromImage(dto.params.fingerprintBytes)
                templateRepository.insertTemplate(template, dto, score)
            }

        return numMissingSaved + numSavedTemplates + numSavedImages
    }

    fun verify(dto: VerifyRequestDto): VerifyResponseDto {
        replayService.checkIfReplay(dto.imageByte)
        return VerifyResponseDto(ResponseStatus.MATCHED) // TODO: Actually fill this out
    }
}
