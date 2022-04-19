package org.kiva.bioauthservice.app

import io.ktor.application.Application
import io.ktor.application.log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.Logging
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.app.config.AppConfig
import org.slf4j.Logger

@KtorExperimentalAPI
fun Application.registerApp(): AppRegistry {

    // Set up Configs
    val appConfig = AppConfig(environment.config)

    // Set up Http Client (Uses CIO engine)
    val httpClientConfig = appConfig.httpConfig.clientConfig
    val httpClient = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        install(HttpTimeout) {
            connectTimeoutMillis = httpClientConfig.connectTimeoutMillis
            requestTimeoutMillis = httpClientConfig.requestTimeoutMillis
            socketTimeoutMillis = httpClientConfig.socketTimeoutMillis
        }
        install(Logging) {
            level = httpClientConfig.logLevel
        }
    }

    // Setup Service
    val appService = AppService(appConfig)

    // Done
    return AppRegistry(this, appService, appConfig, log, httpClient)
}

@KtorExperimentalAPI
data class AppRegistry(
    private val application: Application,
    private val appService: AppService,
    val appConfig: AppConfig,
    val logger: Logger,
    val httpClient: HttpClient
) {
    fun installRoutes() {
        application.routing {
            appRoutes(appService)
        }
    }
}
