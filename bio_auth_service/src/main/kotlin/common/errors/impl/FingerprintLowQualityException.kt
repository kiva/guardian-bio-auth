package org.kiva.bioauthservice.common.errors.impl

import io.ktor.http.HttpStatusCode
import org.kiva.bioauthservice.common.errors.BioAuthException
import org.kiva.bioauthservice.common.errors.BioAuthExceptionCode

class FingerprintLowQualityException(reason: String) :
    BioAuthException(HttpStatusCode.BadRequest, BioAuthExceptionCode.FingerprintLowQuality, reason)
