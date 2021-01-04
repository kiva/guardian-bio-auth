package org.kiva.identityservice.services

import org.kiva.identityservice.domain.Identity
import org.kiva.identityservice.domain.VerifyRequest
import reactor.core.publisher.Mono

/**
 * Receives a verification request, uses that to retrieve candidates for matching from the backend and then matches
 */
interface IVerificationEngine {

    /**
     * Given a verification request, retrieves candidates from backend and matches them to fingerprint
     */
    fun match(verifyRequest: VerifyRequest): Mono<Identity>
}
