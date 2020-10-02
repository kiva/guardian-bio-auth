package org.kiva.identityservice.errorhandling.exceptions

import org.kiva.identityservice.domain.FingerPosition

/**
 * The exception class thrown when there is an error generating template for a given fingerprint image.
 */
class FingerprintTemplateGenerationException
    (private val did: String?, private val position: FingerPosition, private val reason: String) : FingerPrintTemplateException(reason) {
    override fun toString(): String {
        return StringBuilder("Error happened generating template for did:")
                .append(did)
                .append(" position: ").append(position)
                .append(" Reason: ")
                .append(reason).toString()
    }
}
