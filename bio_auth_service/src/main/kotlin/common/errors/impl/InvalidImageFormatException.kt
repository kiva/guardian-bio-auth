package org.kiva.bioauthservice.common.errors.impl

import org.kiva.bioauthservice.common.errors.BioAuthExceptionCode

class InvalidImageFormatException(reason: String) : ValidationException(BioAuthExceptionCode.InvalidImageFormat, reason)
