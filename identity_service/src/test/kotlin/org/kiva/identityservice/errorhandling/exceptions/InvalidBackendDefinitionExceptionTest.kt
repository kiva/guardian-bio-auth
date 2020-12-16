package org.kiva.identityservice.errorhandling.exceptions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * The unit tests for InvalidBackendDefinitionException.
 */
class InvalidBackendDefinitionExceptionTest {
    /**
     * Verifies exception's message.
     */
    @Test
    fun testMessage() {
        val ex1 = InvalidBackendDefinitionException(null)
        assertNotNull(ex1, "Exception should not be null")
        assertNull(ex1.message, "Exception message should be null")
        val ex2 = InvalidBackendDefinitionException("error")
        assertNotNull(ex2, "Exception should not be null")
        assertEquals("error", ex2.message, "Exception message mismatch")
    }
}
