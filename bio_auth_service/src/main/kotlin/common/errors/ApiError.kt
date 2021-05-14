package org.kiva.bioauthservice.common.errors

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import org.kiva.bioauthservice.common.serializers.ZonedDateTimeSerializer
import java.time.ZonedDateTime

@ExperimentalSerializationApi
@Serializable
data class ApiError(
    @Serializable(with = ZonedDateTimeSerializer::class)
    val timestamp: ZonedDateTime,
    val path: String,
    val status: Int,
    val error: String,
    val code: String,
    val message: String?
)
