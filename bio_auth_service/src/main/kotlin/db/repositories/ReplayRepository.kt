package org.kiva.bioauthservice.db.repositories

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.mapTo
import org.kiva.bioauthservice.common.utils.toSha512
import org.kiva.bioauthservice.db.daos.ReplayDao
import org.slf4j.Logger
import java.lang.Exception

class ReplayRepository(private val jdbi: Jdbi, private val logger: Logger) {

    fun addReplay(bytes: ByteArray): ReplayDao {
        val hashValue = bytes.toSha512()
        val replay = jdbi.withHandle<ReplayDao, Exception> {
            it.createQuery(addReplayQuery)
                .bind("hashCode", hashValue)
                .mapTo<ReplayDao>()
                .findOne()
                .get()
        }
        logger.debug("$replay returned by $addReplayQuery")
        return replay
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
