package org.kiva.identityservice.utils

import org.junit.Assert
import org.junit.Test

/**
 * The unit test for verifying SecurityUtils functionality.
 */
class SecurityUtilsTest {

    /**
     * Tests sha256 function.
     *
     * @throws Exception if there is error.
     */
    @Test
    @Throws(Exception::class)
    fun testSha256() {
        val hash1 = sha256("input1")
        val hash2 = sha256("input1")
        Assert.assertNotNull("Null is not expected", hash1)
        Assert.assertNotNull("Null is not expected", hash2)
        Assert.assertEquals("Same inputs should generate same hashes as well!", hash1, hash2)
        val hash3 = sha256("input2")
        Assert.assertNotEquals("Different inputs should have different hashes!", hash1, hash3)
    }

    /**
     * Tests sha256Half function.
     *
     * @throws Exception if there is error.
     */
    @Test
    @Throws(Exception::class)
    fun testSha256Half() {
        val hash1 = sha256("input1")
        val hash2 = sha256Half("input1")
        Assert.assertNotNull("Null is not expected", hash1)
        Assert.assertNotNull("Null is not expected", hash2)
        Assert.assertNotEquals("Different hash methods should have different hashes!", hash1, hash2)
        Assert.assertEquals("The sha256 should generate 64 character digests.", hash1.length, 64)
        Assert.assertEquals("The sha256Half should generate 32 character digests.", hash2.length, 32)

        val hash3 = sha256Half("input1")
        Assert.assertNotNull("Null is not expected", hash3)
        Assert.assertEquals("Same inputs should generate same hashes as well!", hash2, hash3)
        val hash4 = sha256Half("input2")
        Assert.assertNotEquals("Different inputs should have different hashes!", hash2, hash4)
    }

    /**
     * Tests generateHash function.
     *
     * @throws Exception if there is error.
     */
    @Test
    @Throws(Exception::class)
    fun testGenerateHash() {
        val hash1 = generateHash("input1", "pepper1")
        val hash2 = generateHash("input1", "pepper1")
        val hash3 = generateHash("input1", "pepper2")
        val hash4 = generateHash("input2", "pepper1")

        Assert.assertNotNull("Null is not expected", hash1)
        Assert.assertNotNull("Null is not expected", hash2)
        Assert.assertEquals("Same inputs should generate same hash as well!", hash1, hash2)
        Assert.assertEquals("The generated hash length mismatch", hash1.length, 32)

        Assert.assertNotNull("Null is not expected", hash3)
        Assert.assertNotEquals("Different pepper should generate different hash result!", hash1, hash3)

        Assert.assertNotNull("Null is not expected", hash4)
        Assert.assertNotEquals("Different inputs should generate different hash result!", hash1, hash4)

        val hash5 = generateHash("input1", "")
        val hash6 = generateHash("input1", null)
        Assert.assertNotNull("Null is not expected", hash5)
        Assert.assertNotNull("Null is not expected", hash6)
        Assert.assertEquals("Same inputs should generate same hash as well!", hash5, hash6)
        Assert.assertEquals("The generated hash length mismatch", hash5.length, 32)
        Assert.assertEquals("The generated hash length mismatch", hash6.length, 32)
    }

    /**
     * Tests generateHashForList function.
     *
     * @throws Exception if there is error.
     */
    @Test
    @Throws(Exception::class)
    fun testGenerateHashForList() {
        val hash1 = generateHashForList(listOf("input1", "input2"), "pepper1")
        val hash2 = generateHashForList(listOf("input1", "input3"), "pepper1")
        val hash3 = generateHashForList(listOf("input1", "input2"), "pepper2")

        Assert.assertNotNull("Null is not expected", hash1)
        Assert.assertNotNull("Null is not expected", hash2)
        Assert.assertNotNull("Null is not expected", hash3)

        Assert.assertEquals("Mismatch in generated list of hashes!", hash1.size, 2)
        Assert.assertEquals("Mismatch in generated list of hashes!", hash2.size, 2)
        Assert.assertEquals("Mismatch in generated list of hashes!", hash3.size, 2)

        Assert.assertEquals("Same inputs should generate same hash as well!", hash1[0], hash2[0])
        Assert.assertNotEquals("Different input should generate different hash result!", hash1[1], hash2[1])
        Assert.assertNotEquals("Different pepper should generate different hash result!", hash1[0], hash1[1])
    }
}
