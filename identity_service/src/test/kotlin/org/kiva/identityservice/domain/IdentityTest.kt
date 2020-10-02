package org.kiva.identityservice.domain

import org.junit.Assert
import org.junit.Test

/**
 * The unit tests for verifying Identity class.
 */
class IdentityTest {

    /**
     * Tests Identities equality logic.
     */
    @Test
    fun testEquality() {
        val fingerprints: Map<FingerPosition, ByteArray> = mapOf(FingerPosition.fromCode(1) to byteArrayOf(1, 2, 3, 4))

        val id1 = Identity("did1", "national_id1", fingerprints, DataType.TEMPLATE, 1)
        val id2 = Identity("did1", "national_id1", fingerprints, DataType.IMAGE, 2)
        val id3 = Identity("did1", "national_id2", fingerprints, DataType.TEMPLATE, 1)

        Assert.assertNotNull("Identity should not be null", id1)
        Assert.assertNotNull("Identity should not be null", id2)
        Assert.assertNotNull("Identity should not be null", id3)

        Assert.assertEquals("Identities with same national_id are equal", id1, id2)
        Assert.assertNotEquals("Identities with different national_id are not equal", id1, id3)

        Assert.assertEquals("Mismatch in matching score", id1.matchingScore, 0.0, 0.0)
        id1.matchingScore = 15.0
        Assert.assertEquals("Mismatch in matching score", id1.matchingScore, 15.0, 0.0)
    }

    /**
     * Tests Identity to string conversion.
     */
    @Test
    fun testToString() {
        val fingerprints: Map<FingerPosition, ByteArray> = mapOf(FingerPosition.fromCode(1) to byteArrayOf(1, 2, 3, 4))

        val id = Identity("did", "national_id", fingerprints, DataType.TEMPLATE, 1)
        Assert.assertEquals("Unexpected string value", id.toString(), "Identity(id='national_id', fingerprints=[1])")
    }
}
