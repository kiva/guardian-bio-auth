package org.kiva.identityservice.domain

import org.kiva.identityservice.utils.base64ToByte
import java.sql.Timestamp
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

/**
 * Request used to store a fingerprint template directly. Must use the template backend, must provide a template.
 */
data class StoreRequest(

    /**
     * Government ID associated with the fingerprint.
     */
    val voter_id: String?,

    /**
     * Government ID associated with the fingerprint.
     */
    val national_id: String?,

    /**
     * ID of the agent the fingerprint template grants access to.
     */
    @NotBlank
    val did: String,

    @NotEmpty
    val type_id: Int,

    /**
     * A timestamp corresponding to when the fingerprint template was captured.
     */
    @NotBlank
    val capture_date: Timestamp,

    /**
     * Base64 representation of the fingerprint we wish to store.
     */
    @NotBlank
    val templateImage: String,

    /**
     * Position of the finger capture; e.g. left_thumb.
     */
    @NotEmpty
    val position: FingerPosition,

    @NotEmpty
    val quality_score: Int
) {
    var imageByte: ByteArray = base64ToByte(templateImage)
}