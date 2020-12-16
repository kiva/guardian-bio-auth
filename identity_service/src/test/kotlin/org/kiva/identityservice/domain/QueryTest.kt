package org.kiva.identityservice.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

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
        assertNotNull(query1, "Query should not be null")
        assertEquals(DataType.IMAGE, query1.imageType, "Mismatch is query image type")
        assertNotNull(query1.imageByte, "Query imagebytes should not be null")

        val query2 = Query("template", "Image2", FingerPosition.LEFT_THUMB, mapOf(), DataType.IMAGE)
        assertNotNull(query2, "Query should not be null")
        assertEquals(DataType.IMAGE, query2.imageType, "Mismatch is query image type")
        assertNotNull(query2.imageByte, "Query imagebytes should not be null")

        val query3 = Query("template", "Image1", FingerPosition.LEFT_THUMB, mapOf(), DataType.TEMPLATE)
        assertNotNull(query3, "Query should not be null")
        assertEquals(DataType.TEMPLATE, query3.imageType, "Mismatch is query image type")
        assertNotNull(query3.imageByte, "Query imagebytes should not be null")
    }
}
