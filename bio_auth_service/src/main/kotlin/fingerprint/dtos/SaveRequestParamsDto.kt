package org.kiva.bioauthservice.fingerprint.dtos

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import org.kiva.bioauthservice.common.errors.impl.ImageDecodeException
import org.kiva.bioauthservice.common.serializers.ZonedDateTimeSerializer
import org.kiva.bioauthservice.common.utils.base64ToByte
import org.kiva.bioauthservice.fingerprint.enums.DataType
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition
import java.lang.Exception
import java.time.ZonedDateTime

@ExperimentalSerializationApi
@Serializable
data class SaveRequestParamsDto(

    val type_id: Int,

    /**
     * A timestamp corresponding to when the fingerprint was captured.
     */
    @Serializable(with = ZonedDateTimeSerializer::class)
    val capture_date: ZonedDateTime,

    /**
     * Position of the finger capture; e.g. left_thumb.
     */
    val position: FingerPosition,

    /**
     * Base64 representation of the fingerprint image to save a template of
     */
    val image: String = "",

    /**
     * Base 64 representation of the fingerprint template to save
     */
    val template: String = "",

    /**
     * Quality score of the template, if a template is provided.
     */
    val quality_score: Double = 0.0,

    val missing_code: String? = null
) {
    val type: DataType = if (template.isNotBlank()) DataType.TEMPLATE else DataType.IMAGE
    val fingerprintBytes: ByteArray = try {
        (if (type == DataType.TEMPLATE) template else image).base64ToByte()
    } catch (ex: Exception) {
        throw ImageDecodeException(ex.message)
    }
}
