package org.kiva.bioauthservice.fingerprint.dtos

import kotlinx.serialization.Serializable
import org.kiva.bioauthservice.fingerprint.enums.DataType

@Serializable
data class VerifyRequestDto(

    /**
     * Filter to reduce the amount of prints we need to perform fingerprint matching against.
     */
    val filters: VerifyRequestFiltersDto,

    /**
     * Params passed to the backend store which are used to authenticate the verification request. Requires an entry for
     * image (base64 representation of the fingerprint) and position (integer).
     */
    val params: VerifyRequestParamsDto,

    /**
     * Defines the type of submitted fingerprint which is either image or template
     */
    val imageType: DataType = DataType.IMAGE
)
