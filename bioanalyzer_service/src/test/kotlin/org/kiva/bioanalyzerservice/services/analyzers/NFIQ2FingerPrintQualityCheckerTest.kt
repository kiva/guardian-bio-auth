package org.kiva.bioanalyzerservice.services.analyzers

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kiva.bioanalyzerservice.domain.BioType
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.StreamUtils
import reactor.test.StepVerifier

@SpringBootTest
@ExtendWith(SpringExtension::class)
class NFIQ2FingerPrintQualityCheckerTest {

    /**
     * Negative test case for invalid image format which should return empty result.
     */
    @Test
    fun analyzeInvalidFormatType() {
        val bioDataAnalyzer = NFIQ2FingerPrintQualityChecker()

        StepVerifier.create(bioDataAnalyzer.analyze(pngFingerprint, BioType.FINGERPRINT, "text/txt"))
            .expectSubscription()
            .verifyComplete()
    }

    /**
     * Negative test case for empty image bytes which should return error.
     */
    @Test
    fun analyzeEmptyImageBytes() {
        val bioDataAnalyzer = NFIQ2FingerPrintQualityChecker()

        StepVerifier.create(bioDataAnalyzer.analyze(emptyFingerprint, BioType.FINGERPRINT, "image/png"))
            .expectSubscription()
            .expectError(IllegalArgumentException::class.java)
    }

    private var emptyFingerprint: ByteArray = byteArrayOf()
    private var pngFingerprint: ByteArray = StreamUtils.copyToByteArray(ClassPathResource("images/fingerprint.png").inputStream)
}
