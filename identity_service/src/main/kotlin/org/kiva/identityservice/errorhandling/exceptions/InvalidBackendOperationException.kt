package org.kiva.identityservice.errorhandling.exceptions

import org.kiva.identityservice.errorhandling.exceptions.api.ApiException
import org.kiva.identityservice.errorhandling.exceptions.api.ApiExceptionCode
import org.springframework.http.HttpStatus

/**
 * The exception class for invalid operations over a given backend.
 */
class InvalidBackendOperationException
    (private val backend: String, private val operation: String) : ApiException(HttpStatus.BAD_REQUEST, ApiExceptionCode.INVALID_BACKEND_OPERATION) {
        override fun toString(): String {
            return StringBuilder("Backend [")
                    .append(backend)
                    .append("] does not support operation: ").append(operation).toString()
        }
}
