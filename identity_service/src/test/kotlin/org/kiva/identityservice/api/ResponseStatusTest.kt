package org.kiva.identityservice.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * The unit tests for verifying ResponseStatus enum.
 */
class ResponseStatusTest {

    /**
     * Tests enum size.
     */
    @Test
    fun testEnumSize() {
        assertEquals(2, ResponseStatus.values().size, "ResponseStatus enum has two values")
    }

    /**
     * Tests toString.
     */
    @Test
    fun testToString() {
        assertEquals("matched", ResponseStatus.fromCode("MATCHED").toString(), "ToString mismatch")
        assertEquals("not_matched", ResponseStatus.fromCode("NOT_MATCHED").toString(), "ToString mismatch")
    }

    /**
     * Tests from method.
     */
    @Test
    fun testFrom() {
        val rs1 = ResponseStatus.fromCode("matched")
        assertEquals(ResponseStatus.MATCHED, rs1, "Response status mismatch")
        val rs2 = ResponseStatus.fromCode("not_matched")
        assertEquals(ResponseStatus.NOT_MATCHED, rs2, "Response status mismatch")
    }

    /**
     * Tests invalid case for null status code where IllegalArgumentException must be thrown.
     */
    @Test
    fun testFromNullStatusCode() {
        assertThrows<IllegalArgumentException> {
            ResponseStatus.fromCode(null)
        }
    }

    /**
     * Tests invalid case for invalid status code where IllegalArgumentException must be thrown.
     */
    @Test
    fun testFromInvalidStatusCode() {
        assertThrows<IllegalArgumentException> {
            ResponseStatus.fromCode("invalid_code")
        }
    }
}
