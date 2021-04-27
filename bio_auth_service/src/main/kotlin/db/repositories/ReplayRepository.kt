package org.kiva.bioauthservice.db.repositories

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.mapTo
import org.kiva.bioauthservice.db.daos.ReplayDao
import org.kiva.bioauthservice.util.toSha512
import java.lang.Exception

internal interface ReplayRepository {

    val jdbi: Jdbi

    fun addReplay(bytes: ByteArray): ReplayDao {
        val hashValue = bytes.toSha512()
        return jdbi.withHandle<ReplayDao, Exception> {
            it.createQuery(addReplayQuery)
                .bind("hashCode", hashValue)
                .mapTo<ReplayDao>()
                .findOne()
                .get()
        }
    }

    companion object {
        private val addReplayQuery =
            """
            INSERT INTO replays (hash_code) VALUES (:hashCode)
            ON CONFLICT (hash_code) DO UPDATE
            SET count_seen=replays.count_seen + 1, time_seen=now()
            RETURNING *;
            """.trimIndent()
    }
}
