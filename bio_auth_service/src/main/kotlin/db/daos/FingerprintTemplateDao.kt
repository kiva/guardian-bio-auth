package org.kiva.bioauthservice.db.daos

import com.machinezoo.sourceafis.FingerprintTemplate
import org.jdbi.v3.core.mapper.reflect.ColumnName
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition
import java.time.ZonedDateTime

data class FingerprintTemplateDao(
    val id: Int,
    val did: String,
    @ColumnName("type_id") val typeId: Int,
    val version: Int,
    val position: FingerPosition,
    @ColumnName("template_type") val templateType: String,
    val template: FingerprintTemplate?,
    @ColumnName("missing_code") val missingCode: String?,
    @ColumnName("quality_score") val qualityScore: Int?,
    @ColumnName("capture_date") val captureDate: ZonedDateTime?,
    @ColumnName("create_time") val createTime: ZonedDateTime,
    @ColumnName("modify_time") val modifyTime: ZonedDateTime
)
