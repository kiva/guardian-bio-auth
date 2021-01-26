package org.kiva.identityservice.errorhandling.exceptions.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

/**
 * The unit tests for verifying ApiException.
 */
class ApiExceptionTest {

    /**
     * Verifies exception's message and other attributes.
     */
    @Test
    fun testApiException() {
        val ex = ApiException(HttpStatus.BAD_REQUEST, ApiExceptionCode.NoCitizenFound, "error")
        assertNotNull(ex, "Exception should not be null")
        assertEquals("400 BAD_REQUEST \"error\"", ex.message, "Exception message mismatch")
        assertEquals("error", ex.reason, "Exception reason mismatch")
        assertEquals(HttpStatus.BAD_REQUEST, ex.status, "Exception status mismatch")
        assertEquals(ApiExceptionCode.NoCitizenFound, ex.code, "Exception code mismatch")
    }
}
