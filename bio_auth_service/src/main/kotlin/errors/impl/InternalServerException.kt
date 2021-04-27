package org.kiva.bioauthservice.errors.impl

import io.ktor.http.HttpStatusCode
import org.kiva.bioauthservice.errors.BioAuthException
import org.kiva.bioauthservice.errors.BioAuthExceptionCode

class InternalServerException(override val cause: Throwable) :
    BioAuthException(HttpStatusCode.InternalServerError, BioAuthExceptionCode.InternalServerError, "", cause)
