package org.kiva.identityservice.errorhandling.exceptions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.kiva.identityservice.domain.FingerPosition

/**
 * The unit tests for FingerprintTemplateGenerationException.
 */
class FingerprintTemplateGenerationExceptionTest {
    /**
     * Verifies exception's message and toString.
     */
    @Test
    fun testMessage() {
        val ex = FingerprintTemplateGenerationException("did", FingerPosition.LEFT_THUMB, "error")
        assertNotNull(ex, "Exception should not be null")
        assertEquals("error", ex.message, "Exception message mismatch")
        assertEquals("Error happened generating template for did: did position: 6 Reason: error", ex.toString(), "Exception toString mismatch")
    }
}
