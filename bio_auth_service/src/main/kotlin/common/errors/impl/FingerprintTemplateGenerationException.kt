package org.kiva.bioauthservice.common.errors.impl

import org.kiva.bioauthservice.common.errors.BioAuthExceptionCode
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition

class FingerprintTemplateGenerationException(
    private val agentId: String?,
    private val position: FingerPosition,
    reason: String
) : ValidationException(BioAuthExceptionCode.BadRequestError, reason) {
    override fun toString(): String = "Error happened generating template for agentId: $agentId position: $position Reason: $reason"
}
