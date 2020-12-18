package org.kiva.identityservice.errorhandling.exceptions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

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
        assertNotNull(ex, "Exception should not be null")
        assertEquals("error", ex.message, "Exception message mismatch")
    }
}
