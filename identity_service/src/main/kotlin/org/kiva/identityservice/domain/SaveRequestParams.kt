package org.kiva.identityservice.domain

import org.kiva.identityservice.utils.base64ToByte
import org.kiva.identityservice.validators.ImageXorTemplate
import java.sql.Timestamp
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

@ImageXorTemplate
data class SaveRequestParams(

    @NotEmpty
    val type_id: Int,

    /**
     * A timestamp corresponding to when the fingerprint was captured.
     */
    @NotBlank
    val capture_date: Timestamp,

    /**
     * Position of the finger capture; e.g. left_thumb.
     */
    @NotEmpty
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

    val missing_code: String?
) {
    val type: DataType = if (template.isNotBlank()) DataType.TEMPLATE else DataType.IMAGE
    val fingerprintBytes: ByteArray = base64ToByte(if (type == DataType.TEMPLATE) template else image)
}
