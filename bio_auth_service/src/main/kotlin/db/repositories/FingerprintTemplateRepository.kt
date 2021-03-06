package org.kiva.bioauthservice.db.repositories

import com.machinezoo.sourceafis.FingerprintTemplate
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.mapTo
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.EmptyHandling
import org.jdbi.v3.core.statement.StatementContext
import org.kiva.bioauthservice.db.daos.FingerprintTemplateDao
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestDto
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition
import org.slf4j.Logger
import java.sql.ResultSet

// TODO: Use CBOR byte[] serialization of fingerprint template instead of deprecated serialize() and deserialize()
@KtorExperimentalAPI
class FingerprintTemplateRepository(private val jdbi: Jdbi, private val logger: Logger) {

    @ExperimentalSerializationApi
    fun insertTemplate(
        dto: SaveRequestDto,
        template: FingerprintTemplate? = null,
        score: Double? = null,
        templateType: String = "sourceafis",
        templateVersion: Int = 3
    ): Boolean {
        val count = jdbi.withHandle<Int, Exception> {
            it.createUpdate(insertTemplateQuery)
                .bind("agentId", dto.agentId)
                .bind("position", dto.params.position.code)
                .bind("templateType", templateType)
                .bind("typeId", dto.params.type_id)
                .bind("version", templateVersion)
                .bind("captureDate", dto.params.capture_date)
                .bind("missingCode", dto.params.missing_code)
                .bind("template", template?.serialize())
                .bind("qualityScore", score ?: dto.params.quality_score)
                .execute()
        }
        logger.debug("$count rows affected by $insertTemplateQuery")
        return count > 0
    }

    fun getTemplates(agentIds: List<String>, position: FingerPosition): List<FingerprintTemplateDao> {
        val result = jdbi
            .registerColumnMapper(FingerprintTemplateColumnMapper())
            .registerColumnMapper(FingerPositionColumnMapper())
            .withHandle<List<FingerprintTemplateDao>, Exception> {
                it.createQuery(getTemplatesQuery)
                    .bind("position", position.code)
                    .bindList(EmptyHandling.NULL_KEYWORD, "agentIds", agentIds)
                    .mapTo<FingerprintTemplateDao>()
                    .list()
            }
        logger.debug("${result.size} rows returned by $getTemplatesQuery")
        return result
    }

    fun getPositions(agentIds: List<String>): List<FingerPosition> {
        val result = jdbi
            .registerColumnMapper(FingerPositionColumnMapper())
            .withHandle<List<FingerPosition>, Exception> {
                it.createQuery(getPositionsQuery)
                    .bindList(EmptyHandling.NULL_KEYWORD, "agentIds", agentIds)
                    .mapTo<FingerPosition>()
                    .list()
            }
        logger.debug("${result.size} rows returned by $getPositionsQuery")
        return result
    }

    companion object {

        private class FingerprintTemplateColumnMapper : ColumnMapper<FingerprintTemplate?> {
            override fun map(r: ResultSet?, columnNumber: Int, ctx: StatementContext?): FingerprintTemplate? {
                return r?.getString(columnNumber)?.let { FingerprintTemplate().deserialize(it) }
            }
        }

        private class FingerPositionColumnMapper : ColumnMapper<FingerPosition> {
            override fun map(r: ResultSet?, columnNumber: Int, ctx: StatementContext?): FingerPosition {
                return FingerPosition.fromCode(r?.getInt(columnNumber))
            }
        }

        private val insertTemplateQuery =
            """
            INSERT INTO kiva_biometric_template (agent_id,position,template_type,type_id,version,capture_date,missing_code,template,quality_score)
            VALUES (:agentId, :position,:templateType,:typeId,:version,:captureDate,:missingCode,:template,:qualityScore)
            ON CONFLICT ON CONSTRAINT unique_agent_id_position_template_constraint DO UPDATE
            SET version=:version,capture_date=:captureDate,missing_code=:missingCode,template=:template,quality_score=:qualityScore
            """.trimIndent()

        private val getTemplatesQuery =
            """
            SELECT * FROM kiva_biometric_template AS kbt
            WHERE kbt.position=:position
            AND kbt.agent_id IN (<agentIds>)
            LIMIT 1000
            """.trimIndent()

        private val getPositionsQuery =
            """
            SELECT position FROM kiva_biometric_template AS kbt
            WHERE missing_code IS NULL
            AND kbt.agent_id IN (<agentIds>)
            ORDER BY kbt.quality_score DESC
            LIMIT 1000
            """.trimIndent()
    }
}
