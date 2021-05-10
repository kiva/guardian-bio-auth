package org.kiva.bioauthservice.replay

import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.app.config.ReplayConfig
import org.kiva.bioauthservice.db.repositories.ReplayRepository
import org.slf4j.Logger

@KtorExperimentalAPI
class ReplayService(private val logger: Logger, private val replayRepository: ReplayRepository, private val replayConfig: ReplayConfig) {
    fun checkIfReplay(bytes: ByteArray) {
        if (replayConfig.enabled) {
            val replay = replayRepository.addReplay(bytes)
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
