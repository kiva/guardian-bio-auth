package org.kiva.identityservice.services

import org.kiva.identityservice.errorhandling.exceptions.api.FingerprintLowQualityException
import org.kiva.identityservice.errorhandling.exceptions.api.InvalidImageFormatException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
interface IBioAnalyzer {
    @Throws(FingerprintLowQualityException::class, InvalidImageFormatException::class)
    fun analyze(image: String, throwException: Boolean): Mono<Double>
}
