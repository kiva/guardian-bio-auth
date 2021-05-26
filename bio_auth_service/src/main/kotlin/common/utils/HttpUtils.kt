package org.kiva.bioauthservice.common.utils

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpHeaders
import io.ktor.request.header
import java.util.UUID

fun ApplicationCall.requestIdHeader(): String {
    return this.request.header(HttpHeaders.XRequestId) ?: UUID.randomUUID().toString()
}
