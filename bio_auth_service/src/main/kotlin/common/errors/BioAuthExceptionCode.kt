package org.kiva.bioauthservice.common.errors

enum class BioAuthExceptionCode constructor(val msg: String) {
    BadRequestError("Bad request"),
    BioanalyzerServerError("Bioanalyzer server error"),
    FingerprintLowQuality("Given fingerprint is of too low quality to be used for matching. Please recapture"),
    FingerprintMissingNotCaptured("There is no fingerprint for supplied position stored in the database for matching citizen, it was not captured"),
    FingerprintMissingAmputation("There is no fingerprint stored in the database, due to amputation"),
    FingerprintMissingUnableToPrint("There is no fingerprint stored in the database, unable to record fingerprint"),
    FingerprintNoMatch("Fingerprint did not match stored records for citizen supplied through filters"),
    InternalServerError("Unexpected internal error"),
    InvalidFilters("One of your filters is invalid or missing"),
    InvalidParams("One of your params is invalid or missing"),
    InvalidImageEncoding("Invalid image encoding, must be base64 encoded"),
    InvalidImageFormat("Invalid image format, must be one of ..."),
    InvalidPosition("Invalid position, must be one of ..."),
    InvalidTemplate("Invalid template"),
    InvalidTemplateVersion("Invalid template version");
}
