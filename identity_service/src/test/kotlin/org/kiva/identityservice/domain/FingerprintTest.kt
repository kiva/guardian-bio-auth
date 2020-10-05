package org.kiva.identityservice.domain

import java.sql.Timestamp
import org.junit.Assert
import org.junit.Test

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

        Assert.assertNotNull("Fingerprint should not be null", fp1)
        Assert.assertNotNull("Fingerprint should not be null", fp2)
        Assert.assertNotNull("Fingerprint should not be null", fp3)
        Assert.assertNotNull("Fingerprint should not be null", fp4)

        Assert.assertEquals("Fingerprints with same did and finger position are equal", fp1, fp2)
        Assert.assertNotEquals("Fingerprints with different finger position are not equal", fp1, fp3)
        Assert.assertNotEquals("Fingerprints with different did are not equal", fp1, fp4)
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

        Assert.assertNotNull("Fingerprint should not be null", fp1)
        Assert.assertNotNull("Fingerprint should not be null", fp2)
        Assert.assertNotNull("Fingerprint should not be null", fp3)

        val hash1 = fp1.hashCode()
        val hash2 = fp2.hashCode()
        val hash3 = fp3.hashCode()
        Assert.assertNotNull("Fingerprint hashcode should not be null", hash1)
        Assert.assertNotNull("Fingerprint hashcode should not be null", hash2)
        Assert.assertNotNull("Fingerprint hashcode should not be null", hash3)
        Assert.assertEquals("Same Fingerprints should have same hash codes", hash1, hash2)
        Assert.assertNotEquals("Different Fingerprints should have different hash codes", hash1, hash3)
    }
}
