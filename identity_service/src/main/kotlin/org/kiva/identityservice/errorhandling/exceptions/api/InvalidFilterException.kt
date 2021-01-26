package org.kiva.identityservice.errorhandling.exceptions.api

class InvalidFilterException(reason: String? = null) : ValidationError(ApiExceptionCode.InvalidFilters, reason)
