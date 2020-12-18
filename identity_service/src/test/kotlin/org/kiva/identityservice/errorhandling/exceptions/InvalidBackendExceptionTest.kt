package org.kiva.identityservice.errorhandling.exceptions

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * The unit tests for InvalidBackendExceptionTest.
 */
class InvalidBackendExceptionTest {
    /**
     * Verifies exception's message.
     */
    @Test
    fun testMessage() {
        val ex = InvalidBackendException()
        assertNotNull(ex, "Exception should not be null")
        assertNull(ex.message, "Exception message should be null")
    }
}
