package org.kiva.bioauthservice.common.errors.impl

import org.kiva.bioauthservice.common.errors.BioAuthExceptionCode

class InvalidTemplateException(reason: String? = null) : ValidationException(BioAuthExceptionCode.InvalidTemplate, reason)
