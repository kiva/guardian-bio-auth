package org.kiva.identityservice.validators

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.validation.ValidatorFactory

/**
 * The unit tests for verifying ContainsKeys annotation and validator.
 */
@SpringBootTest
@ExtendWith(SpringExtension::class)
@EnableAutoConfiguration(exclude = [R2dbcAutoConfiguration::class])
class ContainsKeysTest {

    @Autowired
    private lateinit var factory: ValidatorFactory

    @Test
    fun testAllOfSuccess() {
        val map = mapOf(Pair("key1", "val1"), Pair("key2", "val2"))
        val violations = factory.validator.validate(TestClassAllOf(map))
        assertTrue(violations.isEmpty())
    }

    @Test
    fun testAllOfNullSuccess() {
        val violations = factory.validator.validate(TestClassAllOf(null))
        assertTrue(violations.isEmpty())
    }

    @Test
    fun testAllOfCompleteFailure() {
        val violations = factory.validator.validate(TestClassAllOf(emptyMap()))
        assertEquals(1, violations.size)
    }

    @Test
    fun testAllOfPartialFailure() {
        val map = mapOf(Pair("key1", "val1"))
        val violations = factory.validator.validate(TestClassAllOf(map))
        assertEquals(1, violations.size)
    }

    @Test
    fun testOneOfCompleteSuccess() {
        val map = mapOf(Pair("key1", "val1"), Pair("key2", "val2"))
        val violations = factory.validator.validate(TestClassOneOf(map))
        assertTrue(violations.isEmpty())
    }

    @Test
    fun testOneOfPartialSuccess() {
        val map = mapOf(Pair("key1", "val1"))
        val violations = factory.validator.validate(TestClassOneOf(map))
        assertTrue(violations.isEmpty())
    }

    @Test
    fun testOneOfNullSuccess() {
        val violations = factory.validator.validate(TestClassOneOf(null))
        assertTrue(violations.isEmpty())
    }

    @Test
    fun testOneOfFailure() {
        val violations = factory.validator.validate(TestClassAllOf(emptyMap()))
        assertEquals(1, violations.size)
    }

    @Test
    fun testCombinedSuccess() {
        val map = mapOf(Pair("key1", "val1"), Pair("key2", "val2"), Pair("key3", "val3"))
        val violations = factory.validator.validate(TestClassCombined(map))
        assertTrue(violations.isEmpty())
    }

    @Test
    fun testCombinedNullSuccess() {
        val violations = factory.validator.validate(TestClassCombined(null))
        assertTrue(violations.isEmpty())
    }

    companion object {

        data class TestClassAllOf(
            @ContainsKeys(allOf = ["key1", "key2"])
            val m: Map<String, String>?
        )

        data class TestClassOneOf(
            @ContainsKeys(oneOf = ["key1", "key2"])
            val m: Map<String, String>?
        )

        data class TestClassCombined(
            @ContainsKeys(allOf = ["key1", "key2"], oneOf = ["key3", "key4"])
            val m: Map<String, String>?
        )
    }
}
