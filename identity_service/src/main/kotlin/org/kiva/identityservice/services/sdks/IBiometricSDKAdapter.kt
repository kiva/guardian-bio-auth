package org.kiva.identityservice.services.sdks

import org.kiva.identityservice.domain.Identity
import org.kiva.identityservice.domain.Query
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * interface that should be implemented by backend biometric matching service
 */
interface IBiometricSDKAdapter {

    val version: Int // SDK version, affects template generation
    val templateType: String

    /**
     * among a list of candidate records check if probe matches any
     */
    fun match(query: Query, people: Flux<Identity>): Flux<Identity>

    fun templatize(image: ByteArray): Mono<Any>
}
