package org.kiva.identityservice.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.sql.Timestamp

/**
 * The unit tests for verifying Fingerprint data class.
 */
class FingerprintTest {

    /**
     * Tests Fingerprint equality logic.
     */
    @Test
    fun testEquality() {

        val fp1 = Fingerprint("voter1", "did1", "national_id1", 1, FingerPosition.LEFT_THUMB, "NA", Timestamp(System.currentTimeMillis()), null)
        val fp2 = Fingerprint("voter1", "did1", "national_id1", 2, FingerPosition.LEFT_THUMB, "NA", Timestamp(System.currentTimeMillis()), null)
        val fp3 = Fingerprint("voter1", "did1", "national_id1", 1, FingerPosition.RIGHT_INDEX, "NA", Timestamp(System.currentTimeMillis()), null)
        val fp4 = Fingerprint("voter2", "did2", "national_id2", 1, FingerPosition.LEFT_THUMB, "NA", Timestamp(System.currentTimeMillis()), null)

        assertNotNull(fp1, "Fingerprint should not be null")
        assertNotNull(fp2, "Fingerprint should not be null")
        assertNotNull(fp3, "Fingerprint should not be null")
        assertNotNull(fp4, "Fingerprint should not be null")

        assertEquals(fp1, fp2, "Fingerprints with same did and finger position are equal")
        assertNotEquals(fp1, fp3, "Fingerprints with different finger position are not equal")
        assertNotEquals(fp1, fp4, "Fingerprints with different did are not equal")
    }

    /**
     * Tests Fingerprint hash logic.
     */
    @Test
    fun testHash() {

        val time = Timestamp(System.currentTimeMillis())
        val fp1 = Fingerprint("voter1", "did1", "national_id1", 1, FingerPosition.LEFT_THUMB, "NA", time, null)
        val fp2 = Fingerprint("voter1", "did1", "national_id1", 1, FingerPosition.LEFT_THUMB, "NA", time, null)
        val fp3 = Fingerprint("voter1", "did1", "national_id1", 1, FingerPosition.RIGHT_INDEX, "NA", time, null)

        assertNotNull(fp1, "Fingerprint should not be null")
        assertNotNull(fp2, "Fingerprint should not be null")
        assertNotNull(fp3, "Fingerprint should not be null")

        val hash1 = fp1.hashCode()
        val hash2 = fp2.hashCode()
        val hash3 = fp3.hashCode()
        assertNotNull(hash1, "Fingerprint hashcode should not be null")
        assertNotNull(hash2, "Fingerprint hashcode should not be null")
        assertNotNull(hash3, "Fingerprint hashcode should not be null")
        assertEquals(hash1, hash2, "Same Fingerprints should have same hash codes")
        assertNotEquals(hash1, hash3, "Different Fingerprints should have different hash codes")
    }
}
