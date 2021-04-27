package org.kiva.bioauthservice.fingerprint

import org.kiva.bioauthservice.db.DbPort
import org.kiva.bioauthservice.fingerprint.dtos.VerifyDto
import org.slf4j.Logger

class FingerprintService(private val logger: Logger, private val dbPort: DbPort) {
    fun verify(dto: VerifyDto) {
        val replay = dbPort.addReplay(dto.imageByte)
        if (replay.countSeen > 1) {
            logger.warn("Possible replay attack seen at ${replay.timeSeen}. Image has been used ${replay.countSeen} times.")
        } else {
            logger.info("Not a replay attack")
        }
    }
}
