package org.kiva.bioauthservice.fingerprint.dtos

import kotlinx.serialization.Serializable

@Serializable
data class PositionsDto(
    @Deprecated("Prefer agentIds over dids")
    val dids: String? = null
) {
    val agentIds: String? = dids
}
