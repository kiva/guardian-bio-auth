package org.kiva.identityservice.errorhandling.exceptions

import org.kiva.identityservice.errorhandling.exceptions.api.ApiException
import org.kiva.identityservice.errorhandling.exceptions.api.ApiExceptionCode
import org.springframework.http.HttpStatus

/**
 * The exception class thrown when there is an error in the bioanalyzer service call.
 */
class BioanalyzerServiceException(reason: String? = null) : ApiException(HttpStatus.BAD_REQUEST, ApiExceptionCode.BIOANALYZER_SERVER_ERROR, reason)
