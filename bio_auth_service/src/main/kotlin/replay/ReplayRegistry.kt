package org.kiva.bioauthservice.replay

import io.ktor.application.Application
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.app.AppRegistry
import org.kiva.bioauthservice.db.DbRegistry

// Getting a child config is an experimental API, subject to future renaming.
@KtorExperimentalAPI
fun Application.registerReplay(appRegistry: AppRegistry, dbRegistry: DbRegistry): ReplayRegistry {
    val replayService = ReplayService(appRegistry.logger, dbRegistry.replayRepository, appRegistry.appConfig.replayConfig)
    return ReplayRegistry(replayService)
}

@KtorExperimentalAPI
data class ReplayRegistry(val replayService: ReplayService)
