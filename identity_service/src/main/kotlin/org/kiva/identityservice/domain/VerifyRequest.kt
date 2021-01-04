package org.kiva.identityservice.domain

import org.kiva.identityservice.utils.base64ToByte
import org.kiva.identityservice.validators.Backend
import org.kiva.identityservice.validators.ContainsKeys
import javax.validation.constraints.NotEmpty

/**
 * VerifyRequest used to check search backend and compare result against a fingerprint
 */
data class VerifyRequest(

    /**
     * Name of backend we wish to search e.g. Template. backends must be declared in the configuration file
     * @see org.kiva.identityservice.services.backends.Backend
     */
    @Backend
    val backend: String,

    /**
     * DEPRECATED
     * Base64 representation of the fingerprint we should check
     */
    val image: String?,

    /**
     * DEPRECATED
     * Position of the finger capture; e.g. left_thumb.
     */
    val position: FingerPosition?,

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
    @NotEmpty
    @ContainsKeys(oneOf = ["nationalId", "voterId", "firstName", "lastName", "dids"])
    val filters: Map<String, String>,

    /**
     * Defines the type of submitted fingerprint which is either image or template
     */
    @NotEmpty
    val imageType: DataType = DataType.IMAGE,

    /**
     * Params passed to the backend store which are used to authenticate the verification request. Requires an entry for
     * image (base64 representation of the fingerprint) and position (integer).
     *
     * While still supporting specifying these as separate top-level fields, this is optional.
     */
    val params: VerifyRequestParams = VerifyRequestParams(image ?: "", position ?: FingerPosition.RIGHT_INDEX)
) {
    var imageByte: ByteArray = base64ToByte(params.image)
}
