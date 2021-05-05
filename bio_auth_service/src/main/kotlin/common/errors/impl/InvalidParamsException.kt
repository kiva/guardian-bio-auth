package org.kiva.bioauthservice.common.errors.impl

import org.kiva.bioauthservice.common.errors.BioAuthExceptionCode

class InvalidParamsException(reason: String? = null) : ValidationException(BioAuthExceptionCode.InvalidParams, reason)
