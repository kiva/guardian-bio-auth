package org.kiva.bioauthservice.db.daos

import org.jdbi.v3.core.mapper.reflect.ColumnName
import java.time.ZonedDateTime

data class ReplayDao(
    val id: Int,
    @ColumnName("hash_code") val hashCode: String,
    @ColumnName("time_seen") val timeSeen: ZonedDateTime,
    @ColumnName("count_seen") val countSeen: Int
)
