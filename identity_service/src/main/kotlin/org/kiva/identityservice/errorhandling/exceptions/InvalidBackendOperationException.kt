package org.kiva.identityservice.errorhandling.exceptions

import org.kiva.identityservice.errorhandling.exceptions.api.ApiException
import org.kiva.identityservice.errorhandling.exceptions.api.ApiExceptionCode
import org.springframework.http.HttpStatus

/**
 * The exception class for invalid operations over a given backend.
 */
class InvalidBackendOperationException(
    private val backend: String,
    private val operation: String
) : ApiException(HttpStatus.BAD_REQUEST, ApiExceptionCode.InvalidBackendOperation) {
    override fun toString(): String = "Backend [$backend] does not support operation: $operation"
}
