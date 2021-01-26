package org.kiva.identityservice.errorhandling.exceptions.api

class InvalidFingerPositionException(reason: String? = null) : ValidationError(ApiExceptionCode.InvalidPosition, reason)
