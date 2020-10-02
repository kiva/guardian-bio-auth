package org.kiva.identityservice.errorhandling.exceptions

import org.junit.Assert
import org.junit.Test

/**
 * The unit tests for InvalidBackendDefinitionException.
 */
class ImageDecodeExceptionTest {
    /**
     * Verifies exception's message.
     */
    @Test
    fun testMessage() {
        val ex1 = ImageDecodeException(null)
        Assert.assertNotNull("Exception should not be null", ex1)
        Assert.assertNull("Exception message should be null", ex1.message)
        val ex2 = ImageDecodeException("error")
        Assert.assertNotNull("Exception should not be null", ex2)
        Assert.assertEquals("Exception message mismatch", ex2.message, "error")
    }
}
