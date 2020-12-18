package org.kiva.identityservice.errorhandling

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * The unit tests for GlobalErrorAttributes.
 */
class GlobalErrorAttributesTest {
    /**
     * Tests getErrorAttributes method.
     */
    @Test
    fun testGetErrorAttributes() {
        val globalErrorAttributes = GlobalErrorAttributes()
        assertNotNull(globalErrorAttributes, "GlobalErrorAttributes map should not be null")
    }
}
