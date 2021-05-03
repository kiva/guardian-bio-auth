package org.kiva.bioauthservice.common.errors.impl

import org.kiva.bioauthservice.common.errors.BioAuthExceptionCode

class FingerprintMissingAmputationException() : ValidationException(BioAuthExceptionCode.FingerprintMissingAmputation)
