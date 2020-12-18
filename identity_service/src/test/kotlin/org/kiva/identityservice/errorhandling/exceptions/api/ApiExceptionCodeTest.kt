package org.kiva.identityservice.errorhandling.exceptions.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * The unit tests for verifying ApiExceptionCode enum.
 */
class ApiExceptionCodeTest {

    /**
     * Tests ApiExceptionCode enum.
     */
    @Test
    fun testApiExceptionCodeEnum() {
        assertEquals(15, ApiExceptionCode.values().size, "ApiExceptionCode enum has 11 values")

        assertEquals("No citizen found for specified filters", ApiExceptionCode.NO_CITIZEN_FOUND.msg, "Mismatch in message")
        assertEquals("Fingerprint did not match stored records for citizen supplied through filters", ApiExceptionCode.FINGERPRINT_NO_MATCH.msg, "Mismatch in message")
        assertEquals("Given fingerprint is of too low quality to be used for matching. Please recapture", ApiExceptionCode.FINGERPRINT_LOW_QUALITY.msg, "Mismatch in message")
        assertEquals("There is no fingerprint for supplied position stored in the database for matching citizen, it was not captured", ApiExceptionCode.FINGERPRINT_MISSING_NOT_CAPTURED.msg, "Mismatch in message")
        assertEquals("There is no fingerprint stored in the database, due to amputation", ApiExceptionCode.FINGERPRINT_MISSING_AMPUTATION.msg, "Mismatch in message")
        assertEquals("There is no fingerprint stored in the database, unable to record fingerprint", ApiExceptionCode.FINGERPRINT_MISSING_UNABLE_TO_PRINT.msg, "Mismatch in message")
        assertEquals("One of your filters is invalid or missing", ApiExceptionCode.INVALID_FILTERS.msg, "Mismatch in message")
        assertEquals("Invalid image encoding, must be base64 encoded", ApiExceptionCode.INVALID_IMAGE_ENCODING.msg, "Mismatch in message")
        assertEquals("Invalid image format, must be one of ...", ApiExceptionCode.INVALID_IMAGE_FORMAT.msg, "Mismatch in message")
        assertEquals("Invalid position, must be one of ...", ApiExceptionCode.INVALID_POSITION.msg, "Mismatch in message")
        assertEquals("The data type is not supported for the backend", ApiExceptionCode.INVALID_DATA_TYPE.msg, "Mismatch in message")
        assertEquals("Invalid template version", ApiExceptionCode.INVALID_TEMPLATE_VERSION.msg, "Mismatch in message")
        assertEquals("Invalid backend name", ApiExceptionCode.INVALID_BACKEND_NAME.msg, "Invalid template version")
        assertEquals("Invalid backend operation", ApiExceptionCode.INVALID_BACKEND_OPERATION.msg, "Mismatch in message")
        assertEquals("Bioanalyzer server error", ApiExceptionCode.BIOANALYZER_SERVER_ERROR.msg, "Mismatch in message")
    }
}
