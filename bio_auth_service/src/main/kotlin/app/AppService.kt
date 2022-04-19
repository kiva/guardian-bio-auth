package org.kiva.bioauthservice.app

import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.kiva.bioauthservice.app.config.AppConfig
import org.kiva.bioauthservice.app.dtos.StatsDto
import java.time.ZonedDateTime

@KtorExperimentalAPI
class AppService(private val appConfig: AppConfig) {

    private val appStartTime: ZonedDateTime = ZonedDateTime.now()

    @ExperimentalSerializationApi
    fun getStats(): StatsDto {
        return StatsDto(
            serviceName = appConfig.name,
            startedAt = appStartTime,
            currentTime = ZonedDateTime.now(),
            versions = listOf("none")
        )
    }
}
