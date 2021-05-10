package org.kiva.bioauthservice.common.errors

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.StatusPages
import io.ktor.response.respond
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import org.kiva.bioauthservice.app.AppRegistry
import org.kiva.bioauthservice.common.errors.impl.BadRequestException
import org.kiva.bioauthservice.common.errors.impl.InternalServerException
import org.slf4j.Logger

@ExperimentalSerializationApi
private suspend fun PipelineContext<Unit, ApplicationCall>.handleError(
    logger: Logger,
    err: BioAuthException
) {
    val errorBody = err.toApiResponseBody(call.request.local.uri)
    logger.error(errorBody.error, err)
    call.respond(err.status, errorBody)
}

@KtorExperimentalAPI
@ExperimentalSerializationApi
fun Application.installErrorHandler(appRegistry: AppRegistry) {
    val logger = appRegistry.logger
    install(StatusPages) {
        exception<BioAuthException> { cause ->
            handleError(logger, cause)
        }
        exception<SerializationException> { cause ->
            handleError(logger, BadRequestException(cause.message?.split("\n")?.first()))
        }
        exception<Throwable> { cause ->
            handleError(logger, InternalServerException(cause))
        }
    }
}
