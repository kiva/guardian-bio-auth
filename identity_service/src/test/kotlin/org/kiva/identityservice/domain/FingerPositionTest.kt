package org.kiva.identityservice.domain

import org.junit.Assert
import org.junit.Test
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
        Assert.assertEquals("FingerPosition mismatch", FingerPosition.fromCode(1), FingerPosition.RIGHT_THUMB)
        Assert.assertEquals("FingerPosition mismatch", FingerPosition.fromCode(2), FingerPosition.RIGHT_INDEX)
        Assert.assertEquals("FingerPosition mismatch", FingerPosition.fromCode(3), FingerPosition.RIGHT_MIDDLE)
        Assert.assertEquals("FingerPosition mismatch", FingerPosition.fromCode(4), FingerPosition.RIGHT_RING)
        Assert.assertEquals("FingerPosition mismatch", FingerPosition.fromCode(5), FingerPosition.RIGHT_PINKY)
        Assert.assertEquals("FingerPosition mismatch", FingerPosition.fromCode(6), FingerPosition.LEFT_THUMB)
        Assert.assertEquals("FingerPosition mismatch", FingerPosition.fromCode(7), FingerPosition.LEFT_INDEX)
        Assert.assertEquals("FingerPosition mismatch", FingerPosition.fromCode(8), FingerPosition.LEFT_MIDDLE)
        Assert.assertEquals("FingerPosition mismatch", FingerPosition.fromCode(9), FingerPosition.LEFT_RING)
        Assert.assertEquals("FingerPosition mismatch", FingerPosition.fromCode(10), FingerPosition.LEFT_PINKY)
    }

    /**
     * Tests FingerPosition valid code.
     */
    @Test
    fun testValidFingerPosition() {
        val fp1 = FingerPosition.fromCode(1)
        val fp2 = FingerPosition.fromCode(2)
        Assert.assertNotNull("FingerPosition should not be null", fp1)
        Assert.assertNotNull("FingerPosition should not be null", fp2)
        Assert.assertNotEquals("Different codes should generate different enums.", fp1, fp2)

        Assert.assertEquals("Unexpected toString value", fp1.toString(), "1")
        Assert.assertEquals("Unexpected toString value", fp2.toString(), "2")
    }

    /**
     * Tests FingerPosition invalid code where InvalidFingerPositionException must be thrown.
     */
    @Test(expected = InvalidFingerPositionException::class)
    fun testInvalidFingerPosition() {
        FingerPosition.fromCode(11)
        Assert.fail("Should not run this code!")
    }
}
