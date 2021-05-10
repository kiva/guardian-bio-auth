package org.kiva.bioauthservice.fingerprint.dtos

import kotlinx.serialization.Serializable
import org.kiva.bioauthservice.common.errors.impl.ImageDecodeException
import org.kiva.bioauthservice.common.utils.base64ToByte
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition
import java.lang.Exception

@Serializable
data class VerifyRequestParamsDto(

    /**
     * Base64 representation of the fingerprint we should check
     */
    val image: String,

    /**
     * Position of the finger capture; e.g. left_thumb.
     */
    val position: FingerPosition
) {
    val imageByte: ByteArray = try {
        image.base64ToByte()
    } catch (ex: Exception) {
        throw ImageDecodeException(ex.message)
    }
}
