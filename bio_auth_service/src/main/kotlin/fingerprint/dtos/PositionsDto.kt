package org.kiva.bioauthservice.fingerprint.dtos

import kotlinx.serialization.Serializable

@Serializable
data class PositionsDto(
    val nationalId: String? = null,
    val voterId: String? = null,
    val dids: String? = null
)
