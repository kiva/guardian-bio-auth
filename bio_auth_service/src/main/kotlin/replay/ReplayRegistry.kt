package org.kiva.bioauthservice.replay

import io.ktor.application.Application
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.db.DbRegistry
import org.slf4j.Logger

// Getting a child config is an experimental API, subject to future renaming.
@KtorExperimentalAPI
fun Application.registerReplay(logger: Logger, dbRegistry: DbRegistry): ReplayRegistry {
    val baseConfig = environment.config.config("replay")
    val replayConfig = ReplayConfig(baseConfig)
    val replayService = ReplayService(logger, dbRegistry.dbAccessor, replayConfig)
    return ReplayRegistry(replayService)
}

@KtorExperimentalAPI
data class ReplayRegistry(val replayService: ReplayService)
