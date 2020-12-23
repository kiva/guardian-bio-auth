package org.kiva.identityservice.services

import org.kiva.identityservice.domain.VerifyRequest
import org.springframework.stereotype.Service

@Service
interface ICheckReplayAttack {
    fun isReplayAttack(verifyRequest: VerifyRequest): Unit
}
