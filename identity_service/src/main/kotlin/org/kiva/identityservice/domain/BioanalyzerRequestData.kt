package org.kiva.identityservice.domain

import javax.validation.constraints.NotBlank

/**
 * The data class for representing the data sent to bioanalyzer service.
 */
data class BioanalyzerRequestData(

    @NotBlank
    val type: String,

    @NotBlank
    val image: String
)
