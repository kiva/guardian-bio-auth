package org.kiva.bioauthservice.common.errors.impl

import io.ktor.http.HttpStatusCode
import org.kiva.bioauthservice.common.errors.BioAuthException
import org.kiva.bioauthservice.common.errors.BioAuthExceptionCode

/**
 * The exception class thrown when there is an error in the bioanalyzer service call.
 */
class BioanalyzerServiceException(reason: String? = null) :
    BioAuthException(HttpStatusCode.BadRequest, BioAuthExceptionCode.BioanalyzerServerError, reason)
