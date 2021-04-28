package org.kiva.bioauthservice.replay

import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.db.DbAccessor
import org.slf4j.Logger

@KtorExperimentalAPI
class ReplayService(private val logger: Logger, private val dbAccessor: DbAccessor, private val replayConfig: ReplayConfig) {
    fun checkIfReplay(bytes: ByteArray) {
        val replay = dbAccessor.addReplay(bytes)
        if (replayConfig.replayEnabled) {
            if (replay.countSeen > 1) {
                logger.warn("Possible replay attack seen at ${replay.timeSeen}. Image has been used ${replay.countSeen} times.")
            } else {
                logger.info("Not a replay attack")
            }
        } else {
            logger.info("Replay attack protection disabled")
        }
    }
}
