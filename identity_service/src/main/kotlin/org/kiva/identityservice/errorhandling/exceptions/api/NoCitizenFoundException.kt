package org.kiva.identityservice.errorhandling.exceptions.api

import org.springframework.http.HttpStatus

class NoCitizenFoundException(reason: String? = null) : ApiException(HttpStatus.BAD_REQUEST, ApiExceptionCode.NO_CITIZEN_FOUND, reason)
