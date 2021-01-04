package org.kiva.identityservice.errorhandling.exceptions.api

class InvalidFilterException(reason: String? = null) : ValidationError(ApiExceptionCode.INVALID_FILTERS, reason)
