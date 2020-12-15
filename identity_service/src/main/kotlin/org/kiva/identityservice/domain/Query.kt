package org.kiva.identityservice.domain

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import org.kiva.identityservice.utils.base64ToByte
import org.kiva.identityservice.validators.Backend

/**
 * Query used to check search backend and compare result against a fingerprint
 */
data class Query(

    /**
     * Name of backend we wish to search e.g. Template. backends must be declared in the configuration file
     * @see org.kiva.identityservice.services.backends.Backend
     */
    @Backend
    val backend: String,

    /**
     * Base64 representation of the fingerprint we should check
     */
    @NotBlank
    val image: String,

    /**
     * Position of the finger capture; e.g. left_thumb.
     */
    @NotEmpty
    val position: FingerPosition,

    /**
     * Filter passed to backend store to reduce the amount of prints we need to perform fingerprint matching against.
     * Only filters declared in the backend definition are legal here and validators declared against them are run here.
     *
     * Examples include:
     *
     * filter by nationalID
     * filters.put("nationalID", "123456")
     *
     * filter by voterID
     * filters.put("voterID", "123456")
     *
     * filter by first and last name
     * filters.put("firstName", "John")
     * filters.put("lastname", "Smith")
     */
    @NotEmpty
    val filters: Map<String, String>,

    /**
     * Defines the type of submitted fingerprint which is either image or template
     */
    @NotEmpty
    val imageType: DataType = DataType.IMAGE,

    /**
     * If provided, return the DID with the score that matches highest with the provided fingerprint.
     */
    val dids: List<String> = emptyList()
) {
    var imageByte: ByteArray = base64ToByte(image)
}
