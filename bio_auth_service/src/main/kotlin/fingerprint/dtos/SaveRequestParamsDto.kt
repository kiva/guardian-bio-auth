package org.kiva.bioauthservice.fingerprint.dtos

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import org.kiva.bioauthservice.common.errors.impl.ImageDecodeException
import org.kiva.bioauthservice.common.serializers.ZonedDateTimeSerializer
import org.kiva.bioauthservice.common.utils.base64ToByte
import org.kiva.bioauthservice.common.utils.decodeHex
import org.kiva.bioauthservice.fingerprint.enums.DataType
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition
import java.lang.Exception
import java.time.ZonedDateTime

@ExperimentalSerializationApi
@Serializable
data class SaveRequestParamsDto(
    val type_id: Int,
    @Serializable(with = ZonedDateTimeSerializer::class)
    val capture_date: ZonedDateTime,
    val position: FingerPosition,
    val image: String = "", // Note: If provided, this is expected to be hex
    val template: String = "", // Note: If provided, this is expected to be base64
    val quality_score: Double = 0.0,
    val missing_code: String? = null
) {
    val type: DataType = if (template.isNotBlank()) DataType.TEMPLATE else DataType.IMAGE
    val fingerprintBytes: ByteArray = try {
        if (type == DataType.TEMPLATE) template.base64ToByte() else image.decodeHex()
    } catch (ex: Exception) {
        throw ImageDecodeException(ex.message)
    }
}
