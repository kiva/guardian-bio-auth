package org.kiva.bioauthservice.fingerprint.dtos

import kotlinx.serialization.Serializable
import org.kiva.bioauthservice.fingerprint.enums.DataType
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition

@Serializable
data class VerifyRequestDto(
    val backend: String,

    /**
     * DEPRECATED
     * Base64 or Hex representation of the fingerprint we should check
     */
    val image: String? = null,

    /**
     * DEPRECATED
     * Position of the finger capture; e.g. left_thumb.
     */
    val position: FingerPosition? = null,

    /**
     * Filter to reduce the amount of prints we need to perform fingerprint matching against.
     */
    val filters: VerifyRequestFiltersDto,

    /**
     * Defines the type of submitted fingerprint which is either image or template
     */
    val imageType: DataType = DataType.IMAGE
) {

    /**
     * Params passed to the backend store which are used to authenticate the verification request. Requires an entry for
     * image (base64 representation of the fingerprint) and position (integer).
     *
     * While still supporting specifying these as separate top-level fields, this is optional.
     */
    val params: VerifyRequestParamsDto = VerifyRequestParamsDto(image ?: "", position ?: FingerPosition.RIGHT_INDEX)
}
