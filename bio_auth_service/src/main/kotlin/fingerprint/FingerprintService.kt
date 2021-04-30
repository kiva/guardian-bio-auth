package org.kiva.bioauthservice.fingerprint

import com.machinezoo.sourceafis.FingerprintCompatibility
import com.machinezoo.sourceafis.FingerprintImage
import com.machinezoo.sourceafis.FingerprintTemplate
import fingerprint.dtos.VerifyResponseDto
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.ExperimentalSerializationApi
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
        return FingerprintTemplateWrapper(FingerprintTemplate(FingerprintImage(image)))
    }

    @ExperimentalSerializationApi
    fun save(bulkDto: BulkSaveRequestDto) {
        // TODO: Top-level check that each fingerprint provided has either a fingerprint/template or a missingCode provided

        // Handle templates
        val templatesToSave: List<SaveRequestDto> = bulkDto.fingerprints.filter { it.params.type == DataType.TEMPLATE }
        val numSavedTemplates = templatesToSave.count { dto: SaveRequestDto ->
            val template = buildTemplate(dto.params.fingerprintBytes)
            templateRepository.insertTemplate(template, dto)
        }

        // Handle images
        val imagesToSave: List<SaveRequestDto> = bulkDto.fingerprints.filter { it.params.type == DataType.IMAGE }
        val numSavedImages = imagesToSave.count { dto: SaveRequestDto ->
            // TODO Calculate score
            val score = 0.0
            val template = buildTemplateFromImage(dto.params.fingerprintBytes)
            templateRepository.insertTemplate(template, dto, score)
        }

        // TODO: Combine and return results
    }

    fun verify(dto: VerifyRequestDto): VerifyResponseDto {
        replayService.checkIfReplay(dto.imageByte)
        return VerifyResponseDto(ResponseStatus.MATCHED) // TODO: Actually fill this out
    }
}
