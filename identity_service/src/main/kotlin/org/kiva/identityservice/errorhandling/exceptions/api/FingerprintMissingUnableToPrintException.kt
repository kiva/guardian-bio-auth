package org.kiva.identityservice.errorhandling.exceptions.api

import org.springframework.http.HttpStatus

class FingerprintMissingUnableToPrintException() : ApiException(HttpStatus.BAD_REQUEST, ApiExceptionCode.FingerprintMissingUnableToPrint)
