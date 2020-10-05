package org.kiva.identityservice.errorhandling.exceptions.api

import org.springframework.http.HttpStatus

class FingerprintLowQualityException(reason: String) : ApiException(HttpStatus.BAD_REQUEST, ApiExceptionCode.FINGERPRINT_LOW_QUALITY, reason)
