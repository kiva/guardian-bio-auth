package org.kiva.identityservice.errorhandling.exceptions

import org.junit.Assert
import org.junit.Test
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
        Assert.assertNotNull("Exception should not be null", ex)
        Assert.assertEquals("Exception message mismatch", ex.message, "error")

        Assert.assertEquals("Exception toString mismatch", ex.toString(), "Error happened generating template for did:did position: 6 Reason: error")
    }
}
