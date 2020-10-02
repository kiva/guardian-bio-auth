package org.kiva.bioanalyzerservice.services.analyzers

import org.hamcrest.CoreMatchers
import org.junit.Assert.assertThat
import org.junit.Test
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
                    assertThat(it as String, CoreMatchers.`is`("image/png"))
                }
                .verifyComplete()

        StepVerifier.create(bioDataAnalyzer.analyze(jpgFingerprint, BioType.FINGERPRINT))
                .expectSubscription()
                .assertNext {
                    assertThat(it as String, CoreMatchers.`is`("image/jpeg"))
                }
                .verifyComplete()

        StepVerifier.create(bioDataAnalyzer.analyze(tiffFingerprint, BioType.FINGERPRINT))
                .expectSubscription()
                .assertNext {
                    assertThat(it as String, CoreMatchers.`is`("image/tiff"))
                }
                .verifyComplete()

        StepVerifier.create(bioDataAnalyzer.analyze(wsqFingerprint, BioType.FINGERPRINT))
                .expectSubscription()
                .assertNext {
                    assertThat(it as String, CoreMatchers.`is`("image/wsq"))
                }
                .verifyComplete()
    }

    private var pngFingerprint: ByteArray = StreamUtils.copyToByteArray(ClassPathResource("images/fingerprint.png").inputStream)
    private var jpgFingerprint: ByteArray = StreamUtils.copyToByteArray(ClassPathResource("images/fingerprint.jpg").inputStream)
    private var tiffFingerprint: ByteArray = StreamUtils.copyToByteArray(ClassPathResource("images/fingerprint.tif").inputStream)
    private var wsqFingerprint: ByteArray = StreamUtils.copyToByteArray(ClassPathResource("images/sample_fingerprint.wsq").inputStream)
}