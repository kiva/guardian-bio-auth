package org.kiva.bioauthservice.common.errors.impl

import org.kiva.bioauthservice.common.errors.BioAuthExceptionCode

class BadRequestException(reason: String?) : ValidationException(BioAuthExceptionCode.BadRequestError, reason)
