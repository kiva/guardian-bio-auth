package org.kiva.identityservice.errorhandling

import org.junit.Assert
import org.junit.Test

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
        Assert.assertNotNull("GlobalErrorAttributes map should not be null", globalErrorAttributes)
    }
}
