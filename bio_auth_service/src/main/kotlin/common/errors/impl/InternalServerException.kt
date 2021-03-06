package org.kiva.bioauthservice.common.errors.impl

import io.ktor.http.HttpStatusCode
import org.kiva.bioauthservice.common.errors.BioAuthException
import org.kiva.bioauthservice.common.errors.BioAuthExceptionCode

class InternalServerException(override val cause: Throwable) :
    BioAuthException(HttpStatusCode.InternalServerError, BioAuthExceptionCode.InternalServerError, "", cause)
