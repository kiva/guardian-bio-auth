package org.kiva.identityservice.errorhandling.exceptions.api

import org.springframework.http.HttpStatus

open class ValidationError(code: ApiExceptionCode, reason: String?) : ApiException(HttpStatus.BAD_REQUEST, code, reason)
