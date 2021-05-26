package org.kiva.bioauthservice.fingerprint.dtos

import kotlinx.serialization.Serializable

@Serializable
data class VerifyRequestFiltersDto(
    val dids: String // Comma-separated list of DIDs
)
