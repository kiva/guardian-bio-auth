package org.kiva.bioauthservice.common.errors.impl

import org.kiva.bioauthservice.common.errors.BioAuthExceptionCode

class InvalidTemplateVersionException(reason: String? = null) : ValidationException(BioAuthExceptionCode.InvalidTemplateVersion, reason)
