package org.kiva.bioauthservice.common.errors.impl

import org.kiva.bioauthservice.common.errors.BioAuthExceptionCode
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition

class FingerprintTemplateGenerationException(
    private val did: String?,
    private val position: FingerPosition,
    reason: String
) : ValidationException(BioAuthExceptionCode.InternalServerError, reason) {
    override fun toString(): String = "Error happened generating template for did: $did position: $position Reason: $reason"
}
