package org.kiva.bioauthservice.fingerprint.dtos

import kotlinx.serialization.Serializable
import org.kiva.bioauthservice.fingerprint.enums.DataType
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition

@Serializable
data class VerifyRequestDto(
    val backend: String,

    /**
     * DEPRECATED
     * Base64 representation of the fingerprint we should check
     */
    val image: String? = null,

    /**
     * DEPRECATED
     * Position of the finger capture; e.g. left_thumb.
     */
    val position: FingerPosition? = null,

    /**
     * Filter to reduce the amount of prints we need to perform fingerprint matching against.
     *
     * Examples include:
     *
     * filter by nationalId
     * filters.put("nationalId", "123456")
     *
     * filter by voterId
     * filters.put("voterId", "123456")
     *
     * filter by comma-separated list of DIDs
     * filters.put("dids", "abcd1234,efgh5678")
     */
    val filters: VerifyRequestFiltersDto,

    /**
     * Defines the type of submitted fingerprint which is either image or template
     */
//    @NotEmpty
    val imageType: DataType = DataType.IMAGE,

    /**
     * Params passed to the backend store which are used to authenticate the verification request. Requires an entry for
     * image (base64 representation of the fingerprint) and position (integer).
     *
     * While still supporting specifying these as separate top-level fields, this is optional.
     */
    var params: VerifyRequestParamsDto = VerifyRequestParamsDto("", FingerPosition.RIGHT_INDEX)
) {
    init {
        if (params.image.isEmpty()) {
            params = VerifyRequestParamsDto(image ?: "", position ?: FingerPosition.RIGHT_INDEX)
        }
    }
}
