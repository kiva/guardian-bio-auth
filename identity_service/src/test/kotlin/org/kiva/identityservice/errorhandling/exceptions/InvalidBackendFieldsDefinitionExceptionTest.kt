package org.kiva.identityservice.errorhandling.exceptions

import org.junit.Assert
import org.junit.Test

/**
 * The unit tests for InvalidBackendFieldsDefinitionException.
 */
class InvalidBackendFieldsDefinitionExceptionTest {
    /**
     * Verifies exception's message and toString.
     */
    @Test
    fun testMessage() {
        val ex = InvalidBackendFieldsDefinitionException("template", listOf("did, name"))
        Assert.assertNotNull("Exception should not be null", ex)

        Assert.assertEquals("Exception message mismatch", ex.message, "template")
        Assert.assertEquals("Exception toString mismatch", ex.toString(), "Backend [template] configuration definition is missing fields: did, name")
    }
}
