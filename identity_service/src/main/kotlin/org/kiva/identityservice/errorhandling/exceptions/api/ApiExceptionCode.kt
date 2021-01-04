package org.kiva.identityservice.errorhandling.exceptions.api

enum class ApiExceptionCode constructor(val msg: String) {
    NO_CITIZEN_FOUND("No citizen found for specified filters"),
    FINGERPRINT_NO_MATCH("Fingerprint did not match stored records for citizen supplied through filters"),
    FINGERPRINT_LOW_QUALITY("Given fingerprint is of too low quality to be used for matching. Please recapture"),
    FINGERPRINT_MISSING_NOT_CAPTURED("There is no fingerprint for supplied position stored in the database for matching citizen, it was not captured"),
    FINGERPRINT_MISSING_AMPUTATION("There is no fingerprint stored in the database, due to amputation"), // @TODO
    FINGERPRINT_MISSING_UNABLE_TO_PRINT("There is no fingerprint stored in the database, unable to record fingerprint"), // @TODO
    INVALID_FILTERS("One of your filters is invalid or missing"),
    INVALID_PARAMS("One of your params is invalid or missing"),
    INVALID_IMAGE_ENCODING("Invalid image encoding, must be base64 encoded"),
    INVALID_IMAGE_FORMAT("Invalid image format, must be one of ..."),
    INVALID_POSITION("Invalid position, must be one of ..."),
    INVALID_DATA_TYPE("The data type is not supported for the backend"),
    INVALID_TEMPLATE_VERSION("Invalid template version"),
    INVALID_BACKEND_NAME("Invalid backend name"),
    INVALID_BACKEND_OPERATION("Invalid backend operation"),
    BIOANALYZER_SERVER_ERROR("Bioanalyzer server error")
}
