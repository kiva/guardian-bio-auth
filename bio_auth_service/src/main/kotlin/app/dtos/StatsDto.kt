package org.kiva.bioauthservice.app.dtos

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import org.kiva.bioauthservice.common.serializers.ZonedDateTimeSerializer
import java.time.ZonedDateTime

@ExperimentalSerializationApi
@Serializable
data class StatsDto(
    val serviceName: String,
    @Serializable(with = ZonedDateTimeSerializer::class)
    val startedAt: ZonedDateTime,
    @Serializable(with = ZonedDateTimeSerializer::class)
    val currentTime: ZonedDateTime,
    val versions: List<String>
)
