package org.kiva.bioauthservice.fingerprint.dtos

import kotlinx.serialization.Serializable
import org.kiva.bioauthservice.fingerprint.enums.DataType
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition
import org.kiva.bioauthservice.util.base64ToByte

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
     * Filter passed to backend store to reduce the amount of prints we need to perform fingerprint matching against.
     * Only filters declared in the backend definition are legal here and validators declared against them are run here.
     *
     * Examples include:
     *
     * filter by nationalId
     * filters.put("nationalId", "123456")
     *
     * filter by voterId
     * filters.put("voterId", "123456")
     *
     * filter by first and last name
     * filters.put("firstName", "John")
     * filters.put("lastname", "Smith")
     *
     * filter by comma-separated list of DIDs
     * filters.put("dids", "abcd1234,efgh5678")
     */
//    @NotEmpty
//    @ContainsKeys(oneOf = ["nationalId", "voterId", "firstName", "lastName", "dids"])
    val filters: Map<String, String>,

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
    var imageByte: ByteArray = params.image.base64ToByte()
    init {
        if (params.image.isEmpty()) {
            params = VerifyRequestParamsDto(image ?: "", position ?: FingerPosition.RIGHT_INDEX)
        }
    }
}
