package org.kiva.bioauthservice.errors.impl

import io.ktor.http.HttpStatusCode
import org.kiva.bioauthservice.errors.BioAuthException
import org.kiva.bioauthservice.errors.BioAuthExceptionCode

open class ValidationException(code: BioAuthExceptionCode, reason: String? = null, cause: Throwable? = null) :
    BioAuthException(HttpStatusCode.BadRequest, code, reason, cause)
