package org.kiva.identityservice.api

data class Response(
    val status: ResponseStatus,
    val id: String? = null,
    @Deprecated("Prefer id over did")
    val did: String? = null,
    val nationalId: String? = null,
    val matchingScore: Double? = null
)
