package org.kiva.identityservice.errorhandling.exceptions.api

import org.springframework.http.HttpStatus

class FingerprintNoMatchException(reason: String? = null) : ApiException(HttpStatus.BAD_REQUEST, ApiExceptionCode.FingerprintNoMatch, reason)
