package org.kiva.bioauthservice.common.errors

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.ExperimentalSerializationApi
import java.lang.RuntimeException
import java.time.Clock
import java.time.ZonedDateTime

open class BioAuthException(
    val status: HttpStatusCode = HttpStatusCode.BadRequest,
    val code: BioAuthExceptionCode,
    val reason: String? = null,
    override val cause: Throwable? = null
) : RuntimeException(reason, cause) {

    @ExperimentalSerializationApi
    fun toApiResponseBody(uri: String): ApiError {
        val now = ZonedDateTime.now(Clock.systemUTC())
        return ApiError(now, uri, status.value, code.name, reason)
    }
}
