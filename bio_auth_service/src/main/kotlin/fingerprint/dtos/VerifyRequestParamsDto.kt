package org.kiva.bioauthservice.fingerprint.dtos

import kotlinx.serialization.Serializable
import org.kiva.bioauthservice.common.errors.impl.ImageDecodeException
import org.kiva.bioauthservice.common.utils.base64ToByte
import org.kiva.bioauthservice.common.utils.hexToByte
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition
import java.lang.Exception

@Serializable
data class VerifyRequestParamsDto(

    /**
     * Base64 or Hex representation of the fingerprint we should check
     */
    val image: String,

    /**
     * Position of the finger capture; e.g. 1.
     */
    val position: FingerPosition
) {
    val imageByte: ByteArray = try {
        try {
            // Try to do a hex decoding first
            image.replace("0x", "").replace("\\x", "").hexToByte()
        } catch (ex1: Exception) {
            // If that fails, try to do a base64 decoding
            image.base64ToByte()
        }
    } catch (ex2: Exception) {
        // If both base64 and hex decoding fails, throw an exception
        throw ImageDecodeException("Provided fingerprint is neither hexadecimal- nor base64-encoded.")
    }
}
