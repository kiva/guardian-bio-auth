package org.kiva.bioauthservice.fingerprint.dtos

import kotlinx.serialization.Serializable

@Serializable
data class VerifyRequestFiltersDto(
    val agentIds: String // Comma-separated list of Agent IDs
)
