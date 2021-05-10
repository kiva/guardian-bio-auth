package org.kiva.bioauthservice.fingerprint.dtos

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@ExperimentalSerializationApi
@Serializable
data class BulkSaveRequestDto(
    val fingerprints: List<SaveRequestDto>
)
