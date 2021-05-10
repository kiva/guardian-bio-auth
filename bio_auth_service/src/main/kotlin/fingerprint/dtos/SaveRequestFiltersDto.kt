package org.kiva.bioauthservice.fingerprint.dtos

import kotlinx.serialization.Serializable

@Serializable
data class SaveRequestFiltersDto(

    /**
     * Government ID associated with the fingerprint.
     */
    val voter_id: String,

    /**
     * Government ID associated with the fingerprint.
     */
    val national_id: String
)
