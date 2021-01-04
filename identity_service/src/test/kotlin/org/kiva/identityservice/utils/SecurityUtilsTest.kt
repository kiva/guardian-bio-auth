package org.kiva.identityservice.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * The unit test for verifying SecurityUtils functionality.
 */
class SecurityUtilsTest {

    /**
     * Tests sha256 function.
     */
    @Test
    fun testSha256() {
        val hash1 = sha256("input1")
        val hash2 = sha256("input1")
        assertNotNull(hash1, "Null is not expected")
        assertNotNull(hash2, "Null is not expected")
        assertEquals(hash1, hash2, "Same inputs should generate same hashes as well!")
        val hash3 = sha256("input2")
        assertNotEquals(hash1, hash3, "Different inputs should have different hashes!")
    }

    /**
     * Tests sha256Half function.
     */
    @Test
    fun testSha256Half() {
        val hash1 = sha256("input1")
        val hash2 = sha256Half("input1")
        assertNotNull(hash1, "Null is not expected")
        assertNotNull(hash2, "Null is not expected")
        assertNotEquals(hash1, hash2, "Different hash methods should have different hashes!")
        assertEquals(64, hash1.length, "The sha256 should generate 64 character digests.")
        assertEquals(32, hash2.length, "The sha256Half should generate 32 character digests.")

        val hash3 = sha256Half("input1")
        assertNotNull(hash3, "Null is not expected")
        assertEquals(hash2, hash3, "Same inputs should generate same hashes as well!")
        val hash4 = sha256Half("input2")
        assertNotEquals(hash2, hash4, "Different inputs should have different hashes!")
    }

    /**
     * Tests generateHash function.
     */
    @Test
    fun testGenerateHash() {
        val hash1 = generateHash("input1", "pepper1")
        val hash2 = generateHash("input1", "pepper1")
        val hash3 = generateHash("input1", "pepper2")
        val hash4 = generateHash("input2", "pepper1")

        assertNotNull(hash1, "Null is not expected")
        assertNotNull(hash2, "Null is not expected")
        assertEquals(hash1, hash2, "Same inputs should generate same hash as well!")
        assertEquals(32, hash1.length, "The generated hash length mismatch")

        assertNotNull(hash3, "Null is not expected")
        assertNotEquals(hash1, hash3, "Different pepper should generate different hash result!")

        assertNotNull(hash4, "Null is not expected")
        assertNotEquals(hash1, hash4, "Different inputs should generate different hash result!")

        val hash5 = generateHash("input1", "")
        val hash6 = generateHash("input1", null)
        assertNotNull(hash5, "Null is not expected")
        assertNotNull(hash6, "Null is not expected")
        assertEquals(hash5, hash6, "Same inputs should generate same hash as well!")
        assertEquals(32, hash5.length, "The generated hash length mismatch")
        assertEquals(32, hash6.length, "The generated hash length mismatch")
    }

    /**
     * Tests generateHashForList function.
     */
    @Test
    fun testGenerateHashForList() {
        val hash1 = generateHashForList(listOf("input1", "input2"), "pepper1")
        val hash2 = generateHashForList(listOf("input1", "input3"), "pepper1")
        val hash3 = generateHashForList(listOf("input1", "input2"), "pepper2")

        assertNotNull(hash1, "Null is not expected")
        assertNotNull(hash2, "Null is not expected")
        assertNotNull(hash3, "Null is not expected")

        assertEquals(2, hash1.size, "Mismatch in generated list of hashes!")
        assertEquals(2, hash2.size, "Mismatch in generated list of hashes!")
        assertEquals(2, hash3.size, "Mismatch in generated list of hashes!")

        assertEquals(hash1[0], hash2[0], "Same inputs should generate same hash as well!")
        assertNotEquals(hash1[1], hash2[1], "Different input should generate different hash result!")
        assertNotEquals(hash1[0], hash1[1], "Different pepper should generate different hash result!")
    }
}
