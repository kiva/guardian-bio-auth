package org.kiva.identityservice.errorhandling.exceptions

import org.junit.Assert
import org.junit.Test

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
        Assert.assertNotNull("Exception should not be null", ex)

        Assert.assertEquals("Exception message mismatch", ex.message, "400 BAD_REQUEST \"Invalid backend operation\"")
        Assert.assertEquals("Exception toString mismatch", ex.toString(), "Backend [template] does not support operation: op1")
    }
}
