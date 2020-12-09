package org.kiva.identityservice.errorhandling.exceptions.api

import org.junit.Assert
import org.junit.Test

/**
 * The unit tests for verifying ApiExceptionCode enum.
 */
class ApiExceptionCodeTest {

    /**
     * Tests ApiExceptionCode enum.
     */
    @Test
    fun testApiExceptionCodeEnum() {
        Assert.assertEquals("ApiExceptionCode enum has 11 values", ApiExceptionCode.values().size, 15)

        Assert.assertEquals("Mismatch in message", ApiExceptionCode.NO_CITIZEN_FOUND.msg,
            "No citizen found for specified filters")
        Assert.assertEquals("Mismatch in message", ApiExceptionCode.FINGERPRINT_NO_MATCH.msg,
            "Fingerprint did not match stored records for citizen supplied through filters")
        Assert.assertEquals("Mismatch in message", ApiExceptionCode.FINGERPRINT_LOW_QUALITY.msg,
            "Given fingerprint is of too low quality to be used for matching. Please recapture")
        Assert.assertEquals("Mismatch in message", ApiExceptionCode.FINGERPRINT_MISSING_NOT_CAPTURED.msg,
            "There is no fingerprint for supplied position stored in the database for matching citizen, it was not captured")
        Assert.assertEquals("Mismatch in message", ApiExceptionCode.FINGERPRINT_MISSING_AMPUTATION.msg,
            "There is no fingerprint stored in the database, due to amputation")
        Assert.assertEquals("Mismatch in message", ApiExceptionCode.FINGERPRINT_MISSING_UNABLE_TO_PRINT.msg,
            "There is no fingerprint stored in the database, unable to record fingerprint")
        Assert.assertEquals("Mismatch in message", ApiExceptionCode.INVALID_FILTERS.msg,
            "One of your filters is invalid or missing")
        Assert.assertEquals("Mismatch in message", ApiExceptionCode.INVALID_IMAGE_ENCODING.msg,
            "Invalid image encoding, must be base64 encoded")
        Assert.assertEquals("Mismatch in message", ApiExceptionCode.INVALID_IMAGE_FORMAT.msg,
            "Invalid image format, must be one of ...")
        Assert.assertEquals("Mismatch in message", ApiExceptionCode.INVALID_POSITION.msg,
            "Invalid position, must be one of ...")
        Assert.assertEquals("Mismatch in message", ApiExceptionCode.INVALID_DATA_TYPE.msg,
            "The data type is not supported for the backend")
        Assert.assertEquals("Mismatch in message", ApiExceptionCode.INVALID_TEMPLATE_VERSION.msg,
            "Invalid template version")
        Assert.assertEquals("Mismatch in message", ApiExceptionCode.INVALID_BACKEND_NAME.msg,
            "Invalid backend name")
        Assert.assertEquals("Mismatch in message", ApiExceptionCode.INVALID_BACKEND_OPERATION.msg,
            "Invalid backend operation")
        Assert.assertEquals("Mismatch in message", ApiExceptionCode.BIOANALYZER_SERVER_ERROR.msg,
            "Bioanalyzer server error")
    }
}
