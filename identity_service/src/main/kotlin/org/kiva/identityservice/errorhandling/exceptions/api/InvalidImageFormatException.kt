package org.kiva.identityservice.errorhandling.exceptions.api

import org.springframework.http.HttpStatus

class InvalidImageFormatException(reason: String) : ApiException(HttpStatus.BAD_REQUEST, ApiExceptionCode.INVALID_IMAGE_FORMAT, reason)
