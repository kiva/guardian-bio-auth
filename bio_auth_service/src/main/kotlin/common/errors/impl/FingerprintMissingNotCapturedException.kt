package org.kiva.bioauthservice.common.errors.impl

import org.kiva.bioauthservice.common.errors.BioAuthExceptionCode

class FingerprintMissingNotCapturedException() : ValidationException(BioAuthExceptionCode.FingerprintMissingNotCaptured)
