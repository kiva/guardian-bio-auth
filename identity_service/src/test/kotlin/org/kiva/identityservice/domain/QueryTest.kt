package org.kiva.identityservice.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * The unit tests for verifying Query data class.
 */
class QueryTest {

    @Test
    fun testQueryWithDefaults() {
        val query = Query("template", "Image1", FingerPosition.LEFT_THUMB, emptyMap())
        assertNotNull(query, "Query should not be null")
        assertEquals(DataType.IMAGE, query.imageType, "Mismatch is query image type")
        assertNotNull(query.imageByte, "Query imagebytes should not be null")
        assertEquals("Image1", query.params.image, "Image should be auto-populated into params")
        assertEquals(FingerPosition.LEFT_THUMB, query.params.position, "Position should be auto-populated into params")
    }

    @Test
    fun testImageQueryWithDefaultParams() {
        val query = Query("template", "Image2", FingerPosition.LEFT_THUMB, emptyMap(), DataType.IMAGE)
        assertNotNull(query, "Query should not be null")
        assertEquals(DataType.IMAGE, query.imageType, "Mismatch is query image type")
        assertNotNull(query.imageByte, "Query imagebytes should not be null")
        assertEquals("Image2", query.params.image, "Image should be auto-populated into params")
        assertEquals(FingerPosition.LEFT_THUMB, query.params.position, "Position should be auto-populated into params")
    }

    @Test
    fun testTemplateQueryWithDefaultParams() {
        val query = Query("template", "Image3", FingerPosition.LEFT_THUMB, emptyMap(), DataType.TEMPLATE)
        assertNotNull(query, "Query should not be null")
        assertEquals(DataType.TEMPLATE, query.imageType, "Mismatch is query image type")
        assertNotNull(query.imageByte, "Query imagebytes should not be null")
        assertEquals("Image3", query.params.image, "Image should be auto-populated into params")
        assertEquals(FingerPosition.LEFT_THUMB, query.params.position, "Position should be auto-populated into params")
    }

    @Test
    fun testQueryWithParams() {
        val params = QueryParams("ImageX", FingerPosition.LEFT_INDEX)
        val query = Query("template", "", FingerPosition.LEFT_THUMB, emptyMap(), DataType.IMAGE, params)
        assertNotNull(query, "Query should not be null")
        assertEquals("ImageX", query.params.image, "Image should not be auto-populated into params")
        assertEquals(FingerPosition.LEFT_INDEX, query.params.position, "Position should not be auto-populated into params")
    }
}
