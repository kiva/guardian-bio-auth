package org.kiva.identityservice.domain

data class SaveRequestFilters(

    /**
     * Government ID associated with the fingerprint.
     */
    val voter_id: String?,

    /**
     * Government ID associated with the fingerprint.
     */
    val national_id: String?
)
