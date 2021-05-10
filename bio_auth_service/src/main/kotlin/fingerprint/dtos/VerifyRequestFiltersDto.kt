package org.kiva.bioauthservice.fingerprint.dtos

import kotlinx.serialization.Serializable

@Serializable
data class VerifyRequestFiltersDto(
    val nationalId: String? = null, // Comma-separated list of nationalIds
    val voterId: String? = null, // Comma-separated list of voter IDs
    val dids: String? = null // Comma-separated list of DIDs
)
