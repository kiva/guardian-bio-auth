package org.kiva.identityservice.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * The unit tests for verifying DataType enum.
 */
class DataTypeTest {

    /**
     * Tests DataType enum size.
     */
    @Test
    fun testEnumSize() {
        assertEquals(2, DataType.values().size, "DataType enum has two values")

        val dt1 = DataType.valueOf("IMAGE")
        assertEquals(DataType.IMAGE, dt1, "DataType mismatch")

        val dt2 = DataType.valueOf("TEMPLATE")
        assertEquals(DataType.TEMPLATE, dt2, "DataType mismatch")
    }
}
