package org.kiva.identityservice.errorhandling.exceptions

import org.kiva.identityservice.errorhandling.exceptions.api.ApiExceptionCode
import org.kiva.identityservice.errorhandling.exceptions.api.ValidationError

class InvalidQueryParamsException(reason: String? = null) : ValidationError(ApiExceptionCode.INVALID_PARAMS, reason)
