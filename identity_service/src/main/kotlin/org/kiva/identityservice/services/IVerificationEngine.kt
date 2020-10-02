package org.kiva.identityservice.services

import org.kiva.identityservice.domain.Identity
import org.kiva.identityservice.domain.Query
import reactor.core.publisher.Mono

/**
 * Main work horse; receives a query, uses that to retrieve candidates for matching from the backend and then matches
 */
interface IVerificationEngine {

    /**
     * Given a query, retrieves candidates from backend and matches them to fingerprint
     */
    fun match(query: Query): Mono<Identity>
}
