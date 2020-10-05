package org.kiva.identityservice.errorhandling.exceptions.api

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

open class ApiException(status: HttpStatus = HttpStatus.BAD_REQUEST, val code: ApiExceptionCode, reason: String? = null) :
        ResponseStatusException(status, reason ?: code.msg)
