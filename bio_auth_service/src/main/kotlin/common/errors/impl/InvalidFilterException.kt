package common.errors.impl

import org.kiva.bioauthservice.common.errors.BioAuthExceptionCode
import org.kiva.bioauthservice.common.errors.impl.ValidationException

class InvalidFilterException(reason: String? = null) : ValidationException(BioAuthExceptionCode.InvalidFilters, reason)
