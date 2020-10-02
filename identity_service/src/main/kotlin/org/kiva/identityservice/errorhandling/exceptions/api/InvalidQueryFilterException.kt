package org.kiva.identityservice.errorhandling.exceptions.api

class InvalidQueryFilterException(reason: String? = null) : ValidationError(ApiExceptionCode.INVALID_FILTERS, reason)
