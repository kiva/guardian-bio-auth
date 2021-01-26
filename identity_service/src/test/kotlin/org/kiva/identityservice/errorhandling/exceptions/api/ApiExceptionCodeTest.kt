package org.kiva.identityservice.errorhandling.exceptions.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

/**
 * The unit tests for verifying ApiExceptionCode enum.
 */
class ApiExceptionCodeTest {

    private fun testNoCitizenFound(code: ApiExceptionCode) {
        val expectedMsg = "No citizen found for specified filters"
        assertEquals(expectedMsg, code.msg, "Mismatch in message")
    }

    private fun testFingerprintNoMatch(code: ApiExceptionCode) {
        val expectedMsg = "Fingerprint did not match stored records for citizen supplied through filters"
        assertEquals(expectedMsg, code.msg, "Mismatch in message")
    }

    private fun testFingerprintLowQuality(code: ApiExceptionCode) {
        val expectedMsg = "Given fingerprint is of too low quality to be used for matching. Please recapture"
        assertEquals(expectedMsg, code.msg, "Mismatch in message")
    }

    private fun testFingerprintMissingNotCaptured(code: ApiExceptionCode) {
        val expectedMsg = "There is no fingerprint for supplied position stored in the database for matching citizen, it was not captured"
        assertEquals(expectedMsg, code.msg, "Mismatch in message")
    }

    private fun testFingerprintMissingAmputation(code: ApiExceptionCode) {
        val expectedMsg = "There is no fingerprint stored in the database, due to amputation"
        assertEquals(expectedMsg, code.msg, "Mismatch in message")
    }

    private fun testFingerprintMissingUnableToPrint(code: ApiExceptionCode) {
        val expectedMsg = "There is no fingerprint stored in the database, unable to record fingerprint"
        assertEquals(expectedMsg, code.msg, "Mismatch in message")
    }

    private fun testInvalidFilters(code: ApiExceptionCode) {
        val expectedMsg = "One of your filters is invalid or missing"
        assertEquals(expectedMsg, code.msg, "Mismatch in message")
    }

    private fun testInvalidParams(code: ApiExceptionCode) {
        val expectedMsg = "One of your params is invalid or missing"
        assertEquals(expectedMsg, code.msg, "Mismatch in message")
    }

    private fun testInvalidImageEncoding(code: ApiExceptionCode) {
        val expectedMsg = "Invalid image encoding, must be base64 encoded"
        assertEquals(expectedMsg, code.msg, "Mismatch in message")
    }

    private fun testInvalidImageFormat(code: ApiExceptionCode) {
        val expectedMsg = "Invalid image format, must be one of ..."
        assertEquals(expectedMsg, code.msg, "Mismatch in message")
    }

    private fun testInvalidPosition(code: ApiExceptionCode) {
        val expectedMsg = "Invalid position, must be one of ..."
        assertEquals(expectedMsg, code.msg, "Mismatch in message")
    }

    private fun testInvalidDataType(code: ApiExceptionCode) {
        val expectedMsg = "The data type is not supported for the backend"
        assertEquals(expectedMsg, code.msg, "Mismatch in message")
    }

    private fun testInvalidTemplateVersion(code: ApiExceptionCode) {
        val expectedMsg = "Invalid template version"
        assertEquals(expectedMsg, code.msg, "Mismatch in message")
    }

    private fun testInvalidBackendName(code: ApiExceptionCode) {
        val expectedMsg = "Invalid backend name"
        assertEquals(expectedMsg, code.msg, "Mismatch in message")
    }

    private fun testInvalidBackendOperation(code: ApiExceptionCode) {
        val expectedMsg = "Invalid backend operation"
        assertEquals(expectedMsg, code.msg, "Mismatch in message")
    }

    private fun testBioanalyzerServerError(code: ApiExceptionCode) {
        val expectedMsg = "Bioanalyzer server error"
        assertEquals(expectedMsg, code.msg, "Mismatch in message")
    }

    /**
     * Tests ApiExceptionCode enum.
     */
    @Test
    fun testApiExceptionCodeEnum() {
        assertEquals(16, ApiExceptionCode.values().size, "ApiExceptionCode enum has ${ApiExceptionCode.values().size} values")
        ApiExceptionCode.values().forEach {
            when (it) {
                ApiExceptionCode.NoCitizenFound -> testNoCitizenFound(it)
                ApiExceptionCode.FingerprintNoMatch -> testFingerprintNoMatch(it)
                ApiExceptionCode.FingerprintLowQuality -> testFingerprintLowQuality(it)
                ApiExceptionCode.FingerprintMissingNotCaptured -> testFingerprintMissingNotCaptured(it)
                ApiExceptionCode.FingerprintMissingAmputation -> testFingerprintMissingAmputation(it)
                ApiExceptionCode.FingerprintMissingUnableToPrint -> testFingerprintMissingUnableToPrint(it)
                ApiExceptionCode.InvalidFilters -> testInvalidFilters(it)
                ApiExceptionCode.InvalidParams -> testInvalidParams(it)
                ApiExceptionCode.InvalidImageEncoding -> testInvalidImageEncoding(it)
                ApiExceptionCode.InvalidImageFormat -> testInvalidImageFormat(it)
                ApiExceptionCode.InvalidPosition -> testInvalidPosition(it)
                ApiExceptionCode.InvalidDataType -> testInvalidDataType(it)
                ApiExceptionCode.InvalidTemplateVersion -> testInvalidTemplateVersion(it)
                ApiExceptionCode.InvalidBackendName -> testInvalidBackendName(it)
                ApiExceptionCode.InvalidBackendOperation -> testInvalidBackendOperation(it)
                ApiExceptionCode.BioanalyzerServerError -> testBioanalyzerServerError(it)
                else -> fail("No test defined for ApiExceptionCode ${it.name}")
            }
        }
    }
}
