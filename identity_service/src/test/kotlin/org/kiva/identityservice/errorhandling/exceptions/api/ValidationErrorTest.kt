package org.kiva.identityservice.errorhandling.exceptions.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

/**
 * The unit tests for verifying ValidationError.
 */
class ValidationErrorTest {

    /**
     * Verifies exception's message and other attributes.
     */
    @Test
    fun testValidationError() {
        val ex = ValidationError(ApiExceptionCode.NoCitizenFound, "error")
        assertNotNull(ex, "Exception should not be null")
        assertEquals("400 BAD_REQUEST \"error\"", ex.message, "Exception message mismatch")
        assertEquals("error", ex.reason, "Exception reason mismatch")
        assertEquals(HttpStatus.BAD_REQUEST, ex.status, "Exception status mismatch")
        assertEquals(ApiExceptionCode.NoCitizenFound, ex.code, "Exception code mismatch")
    }

    /**
     * Verifies exception's for null reason case.
     */
    @Test
    fun testValidationErrorNullReason() {
        val ex = ValidationError(ApiExceptionCode.NoCitizenFound, null)
        assertNotNull(ex, "Exception should not be null")
        assertEquals("400 BAD_REQUEST \"No citizen found for specified filters\"", ex.message, "Exception message mismatch")
        assertEquals("No citizen found for specified filters", ex.reason, "Exception reason mismatch")
        assertEquals(HttpStatus.BAD_REQUEST, ex.status, "Exception status mismatch")
        assertEquals(ApiExceptionCode.NoCitizenFound, ex.code, "Exception code mismatch")
    }
}
