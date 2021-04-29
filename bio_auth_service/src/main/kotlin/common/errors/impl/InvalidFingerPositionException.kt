package org.kiva.bioauthservice.common.errors.impl

import org.kiva.bioauthservice.common.errors.BioAuthExceptionCode

class InvalidFingerPositionException(reason: String? = null) : ValidationException(BioAuthExceptionCode.InvalidPosition, reason)
