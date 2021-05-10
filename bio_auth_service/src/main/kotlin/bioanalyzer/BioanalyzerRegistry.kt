package org.kiva.bioauthservice.bioanalyzer

import io.ktor.application.Application
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.app.AppRegistry

@KtorExperimentalAPI
fun Application.registerBioanalyzer(appRegistry: AppRegistry): BioanalyzerRegistry {
    val bioanalyzerService = BioanalyzerService(appRegistry.logger, appRegistry.appConfig.bioanalyzerConfig, appRegistry.httpClient)
    return BioanalyzerRegistry(bioanalyzerService)
}

@KtorExperimentalAPI
data class BioanalyzerRegistry(val bioanalyzerService: BioanalyzerService)
