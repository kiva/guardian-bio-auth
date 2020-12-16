package org.kiva.identityservice.errorhandling.exceptions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

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
        assertNotNull(ex, "Exception should not be null")

        assertEquals("template", ex.message, "Exception message mismatch")
        assertEquals("Backend [template] configuration definition is missing fields: did, name", ex.toString(), "Exception toString mismatch")
    }
}
