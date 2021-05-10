package org.kiva.bioauthservice.common.errors.impl

import io.ktor.http.HttpStatusCode
import org.kiva.bioauthservice.common.errors.BioAuthException
import org.kiva.bioauthservice.common.errors.BioAuthExceptionCode

open class ValidationException(code: BioAuthExceptionCode, reason: String? = null, cause: Throwable? = null) :
    BioAuthException(HttpStatusCode.BadRequest, code, reason, cause)
