package org.kiva.identityservice.domain

import org.junit.Assert
import org.junit.Test

/**
 * The unit tests for verifying DataType enum.
 */
class DataTypeTest {

    /**
     * Tests DataType enum size.
     */
    @Test
    fun testEnumSize() {
        Assert.assertEquals("DataType enum has two values", DataType.values().size, 2)

        val dt1 = DataType.valueOf("IMAGE")
        Assert.assertEquals("DataType mismatch", dt1, DataType.IMAGE)

        val dt2 = DataType.valueOf("TEMPLATE")
        Assert.assertEquals("DataType mismatch", dt2, DataType.TEMPLATE)
    }
}
