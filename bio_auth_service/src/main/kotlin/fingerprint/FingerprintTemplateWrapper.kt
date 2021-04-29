package org.kiva.bioauthservice.fingerprint

import com.machinezoo.sourceafis.FingerprintTemplate

data class FingerprintTemplateWrapper(
    val fingerprintTemplate: FingerprintTemplate
) {
    val templateType: String = "sourceafis"
    val templateVersion: Int = 3
}
