package org.kiva.bioauthservice.fingerprint.dtos

import kotlinx.serialization.Serializable
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition

@Serializable
data class VerifyParamsDto(

    /**
     * Base64 representation of the fingerprint we should check
     */
    val image: String,

    /**
     * Position of the finger capture; e.g. left_thumb.
     */
    val position: FingerPosition
)
