package org.kiva.bioauthservice.fingerprint

import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.fingerprint.dtos.VerifyRequestDto
import org.kiva.bioauthservice.replay.ReplayService

@KtorExperimentalAPI
class FingerprintService(private val replayService: ReplayService) {
    fun verify(dto: VerifyRequestDto) {
        replayService.checkIfReplay(dto.imageByte)
    }
}
