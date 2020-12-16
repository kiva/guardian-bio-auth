package org.kiva.identityservice.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.kiva.identityservice.errorhandling.exceptions.api.InvalidFingerPositionException

/**
 * The unit tests for verifying FingerPosition class.
 */
class FingerPositionTest {

    /**
     * Tests FingerPosition from method.
     */
    @Test
    fun testFrom() {
        assertEquals(FingerPosition.RIGHT_THUMB, FingerPosition.fromCode(1), "FingerPosition mismatch")
        assertEquals(FingerPosition.RIGHT_INDEX, FingerPosition.fromCode(2), "FingerPosition mismatch")
        assertEquals(FingerPosition.RIGHT_MIDDLE, FingerPosition.fromCode(3), "FingerPosition mismatch")
        assertEquals(FingerPosition.RIGHT_RING, FingerPosition.fromCode(4), "FingerPosition mismatch")
        assertEquals(FingerPosition.RIGHT_PINKY, FingerPosition.fromCode(5), "FingerPosition mismatch")
        assertEquals(FingerPosition.LEFT_THUMB, FingerPosition.fromCode(6), "FingerPosition mismatch")
        assertEquals(FingerPosition.LEFT_INDEX, FingerPosition.fromCode(7), "FingerPosition mismatch")
        assertEquals(FingerPosition.LEFT_MIDDLE, FingerPosition.fromCode(8), "FingerPosition mismatch")
        assertEquals(FingerPosition.LEFT_RING, FingerPosition.fromCode(9), "FingerPosition mismatch")
        assertEquals(FingerPosition.LEFT_PINKY, FingerPosition.fromCode(10), "FingerPosition mismatch")
    }

    /**
     * Tests FingerPosition valid code.
     */
    @Test
    fun testValidFingerPosition() {
        val fp1 = FingerPosition.fromCode(1)
        val fp2 = FingerPosition.fromCode(2)
        assertNotNull(fp1, "FingerPosition should not be null")
        assertNotNull(fp2, "FingerPosition should not be null")
        assertNotEquals(fp1, fp2, "Different codes should generate different enums.")

        assertEquals("1", fp1.toString(), "Unexpected toString value")
        assertEquals("2", fp2.toString(), "Unexpected toString value")
    }

    /**
     * Tests FingerPosition invalid code where InvalidFingerPositionException must be thrown.
     */
    @Test
    fun testInvalidFingerPosition() {
        assertThrows<InvalidFingerPositionException> {
            FingerPosition.fromCode(11)
        }
    }
}
