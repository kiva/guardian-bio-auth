package org.kiva.bioauthservice.fingerprint.dtos

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@ExperimentalSerializationApi
@Serializable
data class SaveRequestDto(

    /**
     * ID of the agent the fingerprint template grants access to.
     */
    val id: String,

    /**
     * IDs to uniquely constrain this fingerprint template to.
     */
    val filters: SaveRequestFiltersDto,

    /**
     * Details of the fingerprint to save.
     */
    val params: SaveRequestParamsDto
)