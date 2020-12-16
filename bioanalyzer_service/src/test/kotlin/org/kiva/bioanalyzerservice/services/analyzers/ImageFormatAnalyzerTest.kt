package org.kiva.bioanalyzerservice.services.analyzers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.kiva.bioanalyzerservice.domain.BioType
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.util.StreamUtils
import reactor.test.StepVerifier

@SpringBootTest
class ImageFormatAnalyzerTest {

    @Test
    fun analyze() {
        val bioDataAnalyzer = ImageFormatAnalyzer()
        StepVerifier.create(bioDataAnalyzer.analyze(pngFingerprint, BioType.FINGERPRINT))
            .expectSubscription()
            .assertNext {
                assertEquals("image/png", it as String)
            }
            .verifyComplete()

        StepVerifier.create(bioDataAnalyzer.analyze(jpgFingerprint, BioType.FINGERPRINT))
            .expectSubscription()
            .assertNext {
                assertEquals("image/jpeg", it as String)
            }
            .verifyComplete()

        StepVerifier.create(bioDataAnalyzer.analyze(tiffFingerprint, BioType.FINGERPRINT))
            .expectSubscription()
            .assertNext {
                assertEquals("image/tiff", it as String)
            }
            .verifyComplete()

        StepVerifier.create(bioDataAnalyzer.analyze(wsqFingerprint, BioType.FINGERPRINT))
            .expectSubscription()
            .assertNext {
                assertEquals("image/wsq", it as String)
            }
            .verifyComplete()
    }

    private var pngFingerprint: ByteArray = StreamUtils.copyToByteArray(ClassPathResource("images/fingerprint.png").inputStream)
    private var jpgFingerprint: ByteArray = StreamUtils.copyToByteArray(ClassPathResource("images/fingerprint.jpg").inputStream)
    private var tiffFingerprint: ByteArray = StreamUtils.copyToByteArray(ClassPathResource("images/fingerprint.tif").inputStream)
    private var wsqFingerprint: ByteArray = StreamUtils.copyToByteArray(ClassPathResource("images/sample_fingerprint.wsq").inputStream)
}
