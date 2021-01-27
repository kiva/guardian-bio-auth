package org.kiva.identityservice.errorhandling.exceptions.api

enum class ApiExceptionCode constructor(val msg: String) {
    NoCitizenFound("No citizen found for specified filters"),
    FingerprintNoMatch("Fingerprint did not match stored records for citizen supplied through filters"),
    FingerprintLowQuality("Given fingerprint is of too low quality to be used for matching. Please recapture"),
    FingerprintMissingNotCaptured("There is no fingerprint for supplied position stored in the database for matching citizen, it was not captured"),
    FingerprintMissingAmputation("There is no fingerprint stored in the database, due to amputation"), // @TODO
    FingerprintMissingUnableToPrint("There is no fingerprint stored in the database, unable to record fingerprint"), // @TODO
    InvalidFilters("One of your filters is invalid or missing"),
    InvalidParams("One of your params is invalid or missing"),
    InvalidImageEncoding("Invalid image encoding, must be base64 encoded"),
    InvalidImageFormat("Invalid image format, must be one of ..."),
    InvalidPosition("Invalid position, must be one of ..."),
    InvalidDataType("The data type is not supported for the backend"),
    InvalidTemplateVersion("Invalid template version"),
    InvalidBackendName("Invalid backend name"),
    InvalidBackendOperation("Invalid backend operation"),
    BioanalyzerServerError("Bioanalyzer server error");
}
