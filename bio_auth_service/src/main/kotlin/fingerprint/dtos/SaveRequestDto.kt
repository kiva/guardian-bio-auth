package org.kiva.bioauthservice.fingerprint.dtos

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@ExperimentalSerializationApi
@Serializable
data class SaveRequestDto(
    val id: String,
    val filters: SaveRequestFiltersDto,
    val params: SaveRequestParamsDto
)
