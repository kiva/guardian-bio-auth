package common.errors.impl

import org.kiva.bioauthservice.common.errors.BioAuthExceptionCode
import org.kiva.bioauthservice.common.errors.impl.ValidationException

class FingerprintNoMatchException(reason: String? = null) : ValidationException(BioAuthExceptionCode.FingerprintNoMatch, reason)
