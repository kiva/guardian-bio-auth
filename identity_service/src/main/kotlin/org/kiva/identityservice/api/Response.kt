package org.kiva.identityservice.api

data class Response(
    val status: ResponseStatus,
    val did: String? = null,
    val nationalId: String? = null,
    val matchingScore: Double? = null
)
