package org.kiva.identityservice.domain

import org.junit.Assert
import org.junit.Test

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
        Assert.assertEquals("Mismatch in type field", bioanalyzerRequestData.type, "type")
        Assert.assertEquals("Mismatch in image field", bioanalyzerRequestData.image, "image")
    }

    /**
     * Tests DataType enum size.
     */
    @Test
    fun testEmptyFields() {
        val bioanalyzerRequestData = BioanalyzerRequestData("", "")
        Assert.assertEquals("Mismatch in type field", bioanalyzerRequestData.type, "")
        Assert.assertEquals("Mismatch in image field", bioanalyzerRequestData.image, "")
    }
}
