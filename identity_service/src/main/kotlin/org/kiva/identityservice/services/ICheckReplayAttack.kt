package org.kiva.identityservice.services

import org.kiva.identityservice.domain.Query
import org.springframework.stereotype.Service

@Service
interface ICheckReplayAttack {

    fun isReplayAttack(query: Query): Unit
}
