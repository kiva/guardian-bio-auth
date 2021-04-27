package org.kiva.bioauthservice.errors

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.StatusPages
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext
import org.kiva.bioauthservice.errors.impl.InternalServerException
import org.slf4j.Logger

private suspend fun PipelineContext<Unit, ApplicationCall>.handleError(
    logger: Logger,
    err: BioAuthException
) {
    val errorBody = err.toApiResponseBody(call.request.local.uri)
    logger.error(errorBody.error, err)
    call.respond(err.status, errorBody)
}

fun Application.installErrorHandler(logger: Logger) {
    install(StatusPages) {
        exception<BioAuthException> { cause ->
            handleError(logger, cause)
        }
        exception<Throwable> { cause ->
            handleError(logger, InternalServerException(cause))
        }
    }
}
