package org.kiva.identityservice.domain

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

data class VerifyRequestParams(

    /**
     * Base64 representation of the fingerprint we should check
     */
    @NotBlank
    val image: String,

    /**
     * Position of the finger capture; e.g. left_thumb.
     */
    @NotEmpty
    val position: FingerPosition
)
