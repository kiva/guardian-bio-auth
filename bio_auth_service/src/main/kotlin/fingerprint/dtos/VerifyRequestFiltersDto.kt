package org.kiva.bioauthservice.fingerprint.dtos

import kotlinx.serialization.Serializable

@Serializable
data class VerifyRequestFiltersDto(
    @Deprecated("Prefer agentIds over dids")
    val dids: String? = null // Comma-separated list of DIDs
) {
    val agentIds: String? = dids
}
