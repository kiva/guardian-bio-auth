package org.kiva.identityservice.errorhandling.exceptions.api

import org.springframework.http.HttpStatus

class FingerprintMissingNotCapturedException() : ApiException(HttpStatus.BAD_REQUEST, ApiExceptionCode.FingerprintMissingNotCaptured)
