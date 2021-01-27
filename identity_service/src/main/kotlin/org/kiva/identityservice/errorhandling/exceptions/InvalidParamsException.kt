package org.kiva.identityservice.errorhandling.exceptions

import org.kiva.identityservice.errorhandling.exceptions.api.ApiExceptionCode
import org.kiva.identityservice.errorhandling.exceptions.api.ValidationError

class InvalidParamsException(reason: String? = null) : ValidationError(ApiExceptionCode.InvalidParams, reason)
