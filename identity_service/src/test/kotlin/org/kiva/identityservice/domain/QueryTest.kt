package org.kiva.identityservice.domain

import org.junit.Assert
import org.junit.Test

/**
 * The unit tests for verifying Query data class.
 */
class QueryTest {

    /**
     * Tests Query data instances.
     */
    @Test
    fun testQueryData() {
        val query1 = Query("template", "Image1", FingerPosition.LEFT_THUMB, mapOf())
        Assert.assertNotNull("Query should not be null", query1)
        Assert.assertEquals("Mismatch is query image type", query1.imageType, DataType.IMAGE)
        Assert.assertNotNull("Query imagebytes should not be null", query1.imageByte)

        val query2 = Query("template", "Image2", FingerPosition.LEFT_THUMB, mapOf(), DataType.IMAGE)
        Assert.assertNotNull("Query should not be null", query2)
        Assert.assertEquals("Mismatch is query image type", query2.imageType, DataType.IMAGE)
        Assert.assertNotNull("Query imagebytes should not be null", query2.imageByte)

        val query3 = Query("template", "Image1", FingerPosition.LEFT_THUMB, mapOf(), DataType.TEMPLATE)
        Assert.assertNotNull("Query should not be null", query3)
        Assert.assertEquals("Mismatch is query image type", query3.imageType, DataType.TEMPLATE)
        Assert.assertNotNull("Query imagebytes should not be null", query3.imageByte)
    }
}
