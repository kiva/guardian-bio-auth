package org.kiva.identityservice.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

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

        assertNotNull(id1, "Identity should not be null")
        assertNotNull(id2, "Identity should not be null")
        assertNotNull(id3, "Identity should not be null")

        assertEquals(id1, id2, "Identities with same national_id are equal")
        assertNotEquals(id1, id3, "Identities with different national_id are not equal")

        assertEquals(id1.matchingScore, 0.0, 0.0, "Mismatch in matching score")
        id1.matchingScore = 15.0
        assertEquals(id1.matchingScore, 15.0, 0.0, "Mismatch in matching score")
    }

    /**
     * Tests Identity to string conversion.
     */
    @Test
    fun testToString() {
        val fingerprints: Map<FingerPosition, ByteArray> = mapOf(FingerPosition.fromCode(1) to byteArrayOf(1, 2, 3, 4))

        val id = Identity("did", "national_id", fingerprints, DataType.TEMPLATE, 1)
        assertEquals("Identity(id='national_id', fingerprints=[1])", id.toString(), "Unexpected string value")
    }
}
