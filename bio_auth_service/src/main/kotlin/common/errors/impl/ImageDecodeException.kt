package org.kiva.bioauthservice.common.errors.impl

import org.kiva.bioauthservice.common.errors.BioAuthExceptionCode

class ImageDecodeException(reason: String?) : ValidationException(BioAuthExceptionCode.InvalidImageEncoding, reason)
