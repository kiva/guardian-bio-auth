package org.kiva.identityservice.domain

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

/**
 * Request used to store a fingerprint. May be provided a fingerprint template or image.
 */
data class SaveRequest(

    /**
     * ID of the agent the fingerprint template grants access to.
     */
    @NotBlank
    val id: String,

    /**
     * IDs to uniquely constrain this fingerprint template to.
     */
    @NotNull
    val filters: SaveRequestFilters,

    /**
     * Details of the fingerprint to save.
     */
    @NotNull
    val params: SaveRequestParams
)
