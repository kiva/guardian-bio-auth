package org.kiva.bioauthservice.errors

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val timestamp: String,
    val path: String,
    val status: Int,
    val error: String,
    val message: String?
)
