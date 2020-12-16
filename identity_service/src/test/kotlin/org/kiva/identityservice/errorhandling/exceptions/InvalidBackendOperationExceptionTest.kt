package org.kiva.identityservice.errorhandling.exceptions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * The unit tests for InvalidBackendOperationException.
 */
class InvalidBackendOperationExceptionTest {
    /**
     * Verifies exception's message and toString.
     */
    @Test
    fun testMessage() {
        val ex = InvalidBackendOperationException("template", "op1")
        assertNotNull(ex, "Exception should not be null")

        assertEquals("400 BAD_REQUEST \"Invalid backend operation\"", ex.message, "Exception message mismatch")
        assertEquals("Backend [template] does not support operation: op1", ex.toString(), "Exception toString mismatch")
    }
}
