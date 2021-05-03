package org.kiva.bioauthservice.db.repositories

import com.machinezoo.sourceafis.FingerprintTemplate
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.mapTo
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.EmptyHandling
import org.jdbi.v3.core.statement.StatementContext
import org.kiva.bioauthservice.common.utils.generateHash
import org.kiva.bioauthservice.common.utils.generateHashForList
import org.kiva.bioauthservice.db.DbConfig
import org.kiva.bioauthservice.db.daos.FingerprintTemplateDao
import org.kiva.bioauthservice.fingerprint.FingerprintTemplateWrapper
import org.kiva.bioauthservice.fingerprint.dtos.PositionsDto
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.VerifyRequestFiltersDto
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition
import org.slf4j.Logger
import java.sql.ResultSet

// TODO: Use CBOR byte[] serialization of fingerprint template instead of deprecated serialize() and deserialize()
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
                .bind("template", templateWrapper.fingerprintTemplate?.serialize())
                .bind("qualityScore", score ?: dto.params.quality_score)
                .execute()
        }
        logger.debug("$count rows affected by $insertTemplateQuery")
        return count > 0
    }

    fun getTemplates(filters: VerifyRequestFiltersDto, position: FingerPosition): List<FingerprintTemplateDao> {
        val dids = filters.dids?.split(',') ?: emptyList()
        val hashedNationalIds = filters.nationalId?.split(',')?.generateHashForList(dbConfig.hashPepper) ?: emptyList()
        val hashedVoterIds = filters.voterId?.split(',')?.generateHashForList(dbConfig.hashPepper) ?: emptyList()
        val result = jdbi
            .registerColumnMapper(FingerprintTemplateColumnMapper())
            .registerColumnMapper(FingerPositionColumnMapper())
            .withHandle<List<FingerprintTemplateDao>, Exception> {
                it.createQuery(getTemplatesQuery)
                    .bind("position", position.code)
                    .bindList(EmptyHandling.NULL_KEYWORD, "dids", dids)
                    .bindList(EmptyHandling.NULL_KEYWORD, "nationalIds", hashedNationalIds)
                    .bindList(EmptyHandling.NULL_KEYWORD, "voterIds", hashedVoterIds)
                    .mapTo<FingerprintTemplateDao>()
                    .list()
            }
        logger.debug("${result.size} rows returned by $getTemplatesQuery")
        return result
    }

    fun getPositions(dto: PositionsDto): List<FingerPosition> {
        val dids = dto.dids?.split(',') ?: emptyList()
        val hashedNationalIds = dto.nationalId?.split(',')?.generateHashForList(dbConfig.hashPepper) ?: emptyList()
        val hashedVoterIds = dto.voterId?.split(',')?.generateHashForList(dbConfig.hashPepper) ?: emptyList()
        val result = jdbi
            .registerColumnMapper(FingerPositionColumnMapper())
            .withHandle<List<FingerPosition>, Exception> {
                it.createQuery(getPositionsQuery)
                    .bindList(EmptyHandling.NULL_KEYWORD, "dids", dids)
                    .bindList(EmptyHandling.NULL_KEYWORD, "nationalIds", hashedNationalIds)
                    .bindList(EmptyHandling.NULL_KEYWORD, "voterIds", hashedVoterIds)
                    .mapTo<FingerPosition>()
                    .list()
            }
        logger.debug("${result.size} rows returned by $getPositionsQuery")
        return result
    }

    companion object {

        private class FingerprintTemplateColumnMapper : ColumnMapper<FingerprintTemplate> {
            override fun map(r: ResultSet?, columnNumber: Int, ctx: StatementContext?): FingerprintTemplate {
                return FingerprintTemplate().deserialize(r?.getString(columnNumber))
            }
        }

        private class FingerPositionColumnMapper : ColumnMapper<FingerPosition> {
            override fun map(r: ResultSet?, columnNumber: Int, ctx: StatementContext?): FingerPosition {
                return FingerPosition.fromCode(r?.getInt(columnNumber))
            }
        }

        private val insertTemplateQuery =
            """
            INSERT INTO kiva_biometric_template (did,position,template_type,type_id,national_id,voter_id,version,capture_date,missing_code,template,quality_score)
            VALUES (:did, :position,:templateType,:typeId,:nationalId,:voterId,:version,:captureDate,:missingCode,:template,:qualityScore)
            ON CONFLICT ON CONSTRAINT unique_did_postion_template_constraint DO UPDATE
            SET national_id=:nationalId,voter_id=:voterId,version=:version,capture_date=:captureDate,missing_code=:missingCode,template=:template,quality_score=:qualityScore
            """.trimIndent()

        private val getTemplatesQuery =
            """
            SELECT * FROM kiva_biometric_template AS kbt
            WHERE kbt.position=:position
            AND ((<dids>) IS NOT NULL OR (<nationalIds>) IS NOT NULL OR (<voterIds>) IS NOT NULL)
            AND ((<dids>) IS NULL OR kbt.did IN (<dids>))
            AND ((<nationalIds>) IS NULL OR kbt.national_id IN (<nationalIds>))
            AND ((<voterIds>) IS NULL OR kbt.voter_id IN (<voterIds>))
            LIMIT 1000
            """.trimIndent()

        private val getPositionsQuery =
            """
            SELECT position FROM kiva_biometric_template AS kbt
            WHERE missing_code IS NULL
            AND ((<dids>) IS NOT NULL OR (<nationalIds>) IS NOT NULL OR (<voterIds>) IS NOT NULL)
            AND ((<dids>) IS NULL OR kbt.did IN (<dids>))
            AND ((<nationalIds>) IS NULL OR kbt.national_id IN (<nationalIds>))
            AND ((<voterIds>) IS NULL OR kbt.voter_id IN (<voterIds>))
            LIMIT 1000
            """.trimIndent()
    }
}
