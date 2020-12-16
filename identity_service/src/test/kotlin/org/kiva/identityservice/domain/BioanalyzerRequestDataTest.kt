package org.kiva.identityservice.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * The unit tests for verifying BioanalyzerRequestData class.
 */
class BioanalyzerRequestDataTest {

    /**
     * Tests DataType enum size.
     */
    @Test
    fun testFields() {
        val bioanalyzerRequestData = BioanalyzerRequestData("type", "image")
        assertEquals("type", bioanalyzerRequestData.type, "Mismatch in type field")
        assertEquals("image", bioanalyzerRequestData.image, "Mismatch in image field")
    }

    /**
     * Tests DataType enum size.
     */
    @Test
    fun testEmptyFields() {
        val bioanalyzerRequestData = BioanalyzerRequestData("", "")
        assertEquals("", bioanalyzerRequestData.type, "Mismatch in type field")
        assertEquals("", bioanalyzerRequestData.image, "Mismatch in image field")
    }
}
