package org.kiva.bioauthservice.fingerprint.dtos

import kotlinx.serialization.Serializable

@Serializable
data class PositionsDto(
    val agentIds: String
)
