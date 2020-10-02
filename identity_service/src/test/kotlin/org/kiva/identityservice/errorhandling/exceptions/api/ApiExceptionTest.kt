package org.kiva.identityservice.errorhandling.exceptions.api

import org.junit.Assert
import org.junit.Test
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
        val ex = ApiException(HttpStatus.BAD_REQUEST, ApiExceptionCode.NO_CITIZEN_FOUND, "error")
        Assert.assertNotNull("Exception should not be null", ex)
        Assert.assertEquals("Exception message mismatch", ex.message, "400 BAD_REQUEST \"error\"")
        Assert.assertEquals("Exception reason mismatch", ex.reason, "error")
        Assert.assertEquals("Exception status mismatch", ex.status, HttpStatus.BAD_REQUEST)
        Assert.assertEquals("Exception code mismatch", ex.code, ApiExceptionCode.NO_CITIZEN_FOUND)
    }
}
