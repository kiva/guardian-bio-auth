package org.kiva.bioauthservice.db.repositories

import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.jdbi.v3.core.Jdbi
import org.kiva.bioauthservice.common.utils.generateHash
import org.kiva.bioauthservice.db.DbConfig
import org.kiva.bioauthservice.fingerprint.FingerprintTemplateWrapper
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestDto
import org.slf4j.Logger

@KtorExperimentalAPI
class FingerprintTemplateRepository(private val jdbi: Jdbi, private val dbConfig: DbConfig, private val logger: Logger) {

    @ExperimentalSerializationApi
    fun insertTemplate(templateWrapper: FingerprintTemplateWrapper, dto: SaveRequestDto, score: Double? = null): Boolean {
        val hashedNationalId = dto.filters.national_id.generateHash(dbConfig.hashPepper)
        val hashedVoterId = dto.filters.voter_id.generateHash(dbConfig.hashPepper)
        val count = jdbi.withHandle<Int, Exception> {
            it.createUpdate(insertTemplateQuery)
                .bind("did", dto.id)
                .bind("position", dto.params.position.code)
                .bind("templateType", templateWrapper.templateType)
                .bind("typeId", dto.params.type_id)
                .bind("nationalId", hashedNationalId)
                .bind("voterId", hashedVoterId)
                .bind("version", templateWrapper.templateVersion)
                .bind("captureDate", dto.params.capture_date)
                .bind("missingCode", dto.params.missing_code)
                .bind("template", templateWrapper.fingerprintTemplate.serialize())
                .bind("qualityScore", score ?: dto.params.quality_score)
                .execute()
        }
        logger.debug("$count rows affected by $insertTemplateQuery")
        return count > 0
    }

    companion object {
        private val insertTemplateQuery =
            """
                INSERT INTO kiva_biometric_template (did,position,template_type,type_id,national_id,voter_id,version,capture_date,missing_code,template,quality_score)
                VALUES (:did, :position,:templateType,:typeId,:nationalId,:voterId,:version,:captureDate,:missingCode,:template,:qualityScore)
                ON CONFLICT ON CONSTRAINT unique_did_postion_template_constraint DO UPDATE
                SET national_id=:nationalId,voter_id=:voterId,version=:version,capture_date=:captureDate,missing_code=:missingCode,template=:template,quality_score=:qualityScore
            """.trimIndent()
    }
}
