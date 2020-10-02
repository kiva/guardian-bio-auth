package org.kiva.identityservice.errorhandling.exceptions

import org.junit.Assert
import org.junit.Test

/**
 * The unit tests for FingerPrintTemplateException.
 */
class FingerPrintTemplateExceptionTest {
    /**
     * Verifies exception's message.
     */
    @Test
    fun testMessage() {
        val ex = FingerPrintTemplateException("error")
        Assert.assertNotNull("Exception should not be null", ex)
        Assert.assertEquals("Exception message mismatch", ex.message, "error")
    }
}
