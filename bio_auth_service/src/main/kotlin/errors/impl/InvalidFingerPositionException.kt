package org.kiva.bioauthservice.errors.impl

import org.kiva.bioauthservice.errors.BioAuthExceptionCode

class InvalidFingerPositionException(reason: String? = null) : ValidationException(BioAuthExceptionCode.InvalidPosition, reason)
