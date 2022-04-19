package org.kiva.bioanalyzerservice.domain

import java.time.ZonedDateTime

data class Stats(
    val serviceName: String,
    val startedAt: ZonedDateTime,
    val currentTime: ZonedDateTime,
    val versions: List<String>
)
