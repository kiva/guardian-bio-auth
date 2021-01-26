package org.kiva.identityservice.errorhandling.exceptions.api

import org.springframework.http.HttpStatus

class FingerprintMissingAmputationException() : ApiException(HttpStatus.BAD_REQUEST, ApiExceptionCode.FingerprintMissingAmputation)
