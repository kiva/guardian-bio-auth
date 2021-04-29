package org.kiva.bioauthservice.fingerprint.dtos

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import org.kiva.bioauthservice.common.serializers.ZonedDateTimeSerializer
import org.kiva.bioauthservice.common.utils.base64ToByte
import org.kiva.bioauthservice.fingerprint.enums.DataType
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition
import java.time.ZonedDateTime

@ExperimentalSerializationApi
@Serializable
data class SaveRequestParamsDto(
    val type_id: Int,
    @Serializable(with = ZonedDateTimeSerializer::class)
    val capture_date: ZonedDateTime,
    val position: FingerPosition,
    val image: String = "",
    val template: String = "",
    val quality_score: Double = 0.0,
    val missing_code: String? = null
) {
    val type: DataType = if (template.isNotBlank()) DataType.TEMPLATE else DataType.IMAGE
    val fingerprintBytes: ByteArray = (if (type == DataType.TEMPLATE) template else image).base64ToByte()
}
