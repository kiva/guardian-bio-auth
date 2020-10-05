package org.kiva.identityservice.api

import org.junit.Assert
import org.junit.Test

/**
 * The unit tests for verifying ResponseStatus enum.
 */
class ResponseStatusTest {

    /**
     * Tests enum size.
     */
    @Test
    fun testEnumSize() {
        Assert.assertEquals("ResponseStatus enum has two values", ResponseStatus.values().size, 2)
    }

    /**
     * Tests toString.
     */
    @Test
    fun testToString() {
        Assert.assertEquals("ToString mismatch", ResponseStatus.fromCode("MATCHED").toString(), "matched")
        Assert.assertEquals("ToString mismatch", ResponseStatus.fromCode("NOT_MATCHED").toString(), "not_matched")
    }

    /**
     * Tests from method.
     */
    @Test
    fun testFrom() {
        val rs1 = ResponseStatus.fromCode("matched")
        Assert.assertEquals("Response status mismatch", rs1, ResponseStatus.MATCHED)
        val rs2 = ResponseStatus.fromCode("not_matched")
        Assert.assertEquals("Response status mismatch", rs2, ResponseStatus.NOT_MATCHED)
    }

    /**
     * Tests invalid case for null status code where IllegalArgumentException must be thrown.
     */
    @Test(expected = IllegalArgumentException::class)
    fun testFromNullStatusCode() {
        ResponseStatus.fromCode(null)
        Assert.fail("Should not run this code!")
    }

    /**
     * Tests invalid case for invalid status code where IllegalArgumentException must be thrown.
     */
    @Test(expected = IllegalArgumentException::class)
    fun testFromInvalidStatusCode() {
        ResponseStatus.fromCode("invalid_code")
        Assert.fail("Should not run this code!")
    }
}
