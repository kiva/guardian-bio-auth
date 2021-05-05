package org.kiva.bioauthservice.bioanalyzer.dtos

import kotlinx.serialization.Serializable

/**
 * The data class for representing the data sent to bioanalyzer service.
 */
@Serializable
data class BioanalyzerRequestDto(
    val type: String,
    val image: String
)
