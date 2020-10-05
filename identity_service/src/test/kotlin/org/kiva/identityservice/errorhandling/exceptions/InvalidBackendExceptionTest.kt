package org.kiva.identityservice.errorhandling.exceptions

import org.junit.Assert
import org.junit.Test

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
        Assert.assertNotNull("Exception should not be null", ex)
        Assert.assertNull("Exception message should be null", ex.message)
    }
}
