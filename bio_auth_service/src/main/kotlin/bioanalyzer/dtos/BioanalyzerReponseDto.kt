package org.kiva.bioauthservice.bioanalyzer.dtos

import kotlinx.serialization.Serializable

@Serializable
data class BioanalyzerReponseDto(
    val quality: Double? = null,
    val format: String? = null,
    val resolution: Int? = null
)
