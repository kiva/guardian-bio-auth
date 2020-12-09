package org.kiva.identityservice.services

import com.machinezoo.sourceafis.FingerprintImage
import com.machinezoo.sourceafis.FingerprintTemplate
import com.nhaarman.mockitokotlin2.any
import java.time.Duration
import java.util.concurrent.TimeoutException
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test
import org.kiva.identityservice.EnvironmentsTest
import org.kiva.identityservice.domain.DataType
import org.kiva.identityservice.errorhandling.exceptions.InvalidBackendException
import org.kiva.identityservice.errorhandling.exceptions.api.FingerprintLowQualityException
import org.kiva.identityservice.errorhandling.exceptions.api.FingerprintNoMatchException
import org.kiva.identityservice.errorhandling.exceptions.api.InvalidImageFormatException
import org.kiva.identityservice.errorhandling.exceptions.api.InvalidQueryFilterException
import org.kiva.identityservice.generateIdentities
import org.kiva.identityservice.generateIdentity
import org.kiva.identityservice.generateQuery
import org.kiva.identityservice.generateTemplateQuery
import org.kiva.identityservice.services.backends.IBackend
import org.kiva.identityservice.services.backends.IBackendManager
import org.kiva.identityservice.services.sdks.sourceafis.SourceAFISFingerprintSDKAdapter
import org.kiva.identityservice.utils.base64ToByte
import org.kiva.identityservice.utils.loadBase64FromResource
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@SpringBootTest
class VerificationEngineTest {

    /**
     * Negative test for wrong query image type.
     */
    @Test
    @Throws(Exception::class)
    fun testFailWrongContentType() {
        Mockito.doReturn(backend).`when`(backendManager)!!.getbyName(Mockito.anyString())
        val sdk = Mockito.mock(SourceAFISFingerprintSDKAdapter::class.java)

        val query1 = query.copy(image = loadBase64FromResource(TEXT1_FILE))
        val verificationEngine = VerificationEngine(backendManager, sdk, listOf("image/jpeg", "image/bmp", "image/png"), 20, checkReplayAttack, bioAnalyzer)
        StepVerifier.create(verificationEngine.match(query1))
            .expectSubscription()
            .verifyError(InvalidImageFormatException::class.java)
    }

    /**
     * Negative test for wrong query image when the submitted image is template type.
     */
    @Test
    @Throws(Exception::class)
    fun testFailWrongTemplateContentType() {
        Mockito.doReturn(backend).`when`(backendManager)!!.getbyName(Mockito.anyString())
        val sdk = Mockito.mock(SourceAFISFingerprintSDKAdapter::class.java)

        val templateQuery = generateQuery(DataType.TEMPLATE)
        val verificationEngine = VerificationEngine(backendManager, sdk, listOf("image/jpeg", "image/bmp", "image/png"), 20, checkReplayAttack, bioAnalyzer)
        StepVerifier.create(verificationEngine.match(templateQuery))
            .expectSubscription()
            .verifyError(InvalidImageFormatException::class.java)
    }

    /**
     * Negative test for invalid backend name.
     */
    @Test
    @Throws(Exception::class)
    fun testFailInvalidBackendName() {
        Mockito.doReturn(backend).`when`(backendManager)!!.getbyName(Mockito.anyString())
        val sdk = Mockito.mock(SourceAFISFingerprintSDKAdapter::class.java)

        Mockito.doThrow(InvalidBackendException()).`when`(backendManager).validateQuery(query)

        val verificationEngine = VerificationEngine(backendManager, sdk, listOf("image/jpeg", "image/bmp", "image/png"), 20, checkReplayAttack, bioAnalyzer)
        StepVerifier.create(verificationEngine.match(query))
            .expectSubscription()
            .verifyError(InvalidBackendException::class.java)
    }

    /**
     * Negative test for invalid query filter.
     */
    @Test
    @Throws(Exception::class)
    fun testFailInvalidQueryFilter() {
        Mockito.doReturn(backend).`when`(backendManager)!!.getbyName(Mockito.anyString())
        val sdk = Mockito.mock(SourceAFISFingerprintSDKAdapter::class.java)

        Mockito.doThrow(InvalidQueryFilterException()).`when`(backendManager).validateQuery(query)

        val verificationEngine = VerificationEngine(backendManager, sdk, listOf("image/jpeg", "image/bmp", "image/png"), 20, checkReplayAttack, bioAnalyzer)
        StepVerifier.create(verificationEngine.match(query))
            .expectSubscription()
            .verifyError(InvalidQueryFilterException::class.java)
    }

    /**
     * Negative test when there is no fingerprint match as the matching threshold is set to a high value
     */
    @Test
    @Throws(Exception::class)
    fun testFailNoFingerprintMatch() {

        Mockito.doReturn(backend).`when`(backendManager).getbyName(Mockito.anyString())
        val sdk = SourceAFISFingerprintSDKAdapter(100000.0)

        val identity = generateIdentity(DID, query.imageByte)
        Mockito.doReturn(Flux.just(identity)).`when`(backend).search(any(), any(), any())

        val emptyMono: Mono<Any> = Mono.empty()
        Mockito.doReturn(emptyMono).`when`(bioAnalyzer).analyze(any(), any())

        val verificationEngine = VerificationEngine(backendManager, sdk, listOf("image/jpeg", "image/bmp", "image/png"), 20, checkReplayAttack, bioAnalyzer)
        StepVerifier.create(verificationEngine.match(query))
            .expectSubscription()
            .verifyError(FingerprintNoMatchException::class.java)
    }

    /**
     * Negative test when there is no match. Also it throws FingerprintLowQualityException because the submitted
     * fingerprint has very low quality. To achieve that, we set the matching threshold to a high value, so, there would
     * be no match for sure and bioanalyzer will run.
     */
    @Test
    @Throws(Exception::class)
    fun testFailVeryLowQualityImage() {
        Mockito.doReturn(backend).`when`(backendManager)!!.getbyName(Mockito.anyString())
        val sdk = SourceAFISFingerprintSDKAdapter(100000.0)

        val identity = generateIdentity(DID, query.imageByte)
        Mockito.doReturn(Flux.just(identity)).`when`(backend).search(any(), any(), any())

        val error: Mono<Any> = Mono.error(FingerprintLowQualityException("error"))
        Mockito.doReturn(error).`when`(bioAnalyzer).analyze(any(), any())

        val verificationEngine = VerificationEngine(backendManager, sdk, listOf("image/jpeg", "image/bmp", "image/png"), 20, checkReplayAttack, bioAnalyzer)
        StepVerifier.create(verificationEngine.match(query))
            .expectSubscription()
            .verifyError(FingerprintLowQualityException::class.java)
    }

    /**
     * Negative test when there is no match. Also it throws FingerprintLowQualityException because the submitted
     * fingerprint has very low quality. To achieve that, we set the matching threshold to a high value, so, there would
     * be no match for sure and bioanalyzer will fail fo sure as the request id header is not set. Here we expect
     * FingerprintNoMatchException since exception thrown in bioAnalyzer.analyze as long as it is not
     * FingerprintLowQualityException.
     */
    @Test
    @Throws(Exception::class)
    fun testFailBioanalyzerServiceError() {
        Mockito.doReturn(backend).`when`(backendManager)!!.getbyName(Mockito.anyString())
        val sdk = SourceAFISFingerprintSDKAdapter(100000.0)

        val identity = generateIdentity(DID, query.imageByte)
        Mockito.doReturn(Flux.just(identity)).`when`(backend).search(any(), any(), any())

        val bioAnalyzer1 = BioAnalyzer(listOf("image/jpeg", "image/bmp", "image/png"))
        environmentsTest.injectEnvironmentVariable("BIOANALYZER_ENABLED", "true")

        val verificationEngine = VerificationEngine(backendManager, sdk, listOf("image/jpeg", "image/bmp", "image/png"), 20, checkReplayAttack, bioAnalyzer1)
        StepVerifier.create(verificationEngine.match(query))
            .expectSubscription()
            .verifyError(FingerprintNoMatchException::class.java)
    }

    /**
     * Negative test when there is no match. Calling bioanalyzer, however, it times out and consequently returns
     * FingerprintNoMatchException.
     */
    @Test
    @Throws(Exception::class)
    fun testFailBioanalyzerServiceTimeout() {
        Mockito.doReturn(backend).`when`(backendManager)!!.getbyName(Mockito.anyString())
        val sdk = SourceAFISFingerprintSDKAdapter(100000.0)

        val identity = generateIdentity(DID, query.imageByte)
        Mockito.doReturn(Flux.just(identity)).`when`(backend).search(any(), any(), any())

        val error: Mono<Any> = Mono.error(TimeoutException())
        Mockito.doReturn(error).`when`(bioAnalyzer).analyze(any(), any())

        val verificationEngine = VerificationEngine(backendManager, sdk, listOf("image/jpeg", "image/bmp", "image/png"), 20, checkReplayAttack, bioAnalyzer)
        StepVerifier.create(verificationEngine.match(query))
            .expectSubscription()
            .verifyError(FingerprintNoMatchException::class.java)
    }

    /**
     * Negative test when there is no match. Calling the bioanalyzer service, it responses after ten minutes. The
     * verification engine, however, times out in five seconds and returns no fingerprint match error.
     */
    @Test
    @Throws(Exception::class)
    fun testBioanalyzerServiceLateAnswer() {
        Mockito.doReturn(backend).`when`(backendManager)!!.getbyName(Mockito.anyString())
        val sdk = SourceAFISFingerprintSDKAdapter(100000.0)

        val identity = generateIdentity(DID, query.imageByte)
        Mockito.doReturn(Flux.just(identity)).`when`(backend).search(any(), any(), any())

        val answer = Mono.just(80).delayElement(Duration.ofSeconds(600L))
        Mockito.doReturn(answer).`when`(bioAnalyzer).analyze(any(), any())

        val verificationEngine = VerificationEngine(backendManager, sdk, listOf("image/jpeg", "image/bmp", "image/png"), 20, checkReplayAttack, bioAnalyzer)
        StepVerifier.create(verificationEngine.match(query))
            .expectSubscription()
            .verifyError(FingerprintNoMatchException::class.java)
    }

    /**
     * Tests when there is a match and it is verified as well. This test also calls match against the template version
     * of fingerprint image and verifies that the matching score for template is equal to matching score of image.
     */
    @Test
    @Throws(Exception::class)
    fun testMatchVerifyAgainstOnePerson() {
        Mockito.doReturn(backend).`when`(backendManager).getbyName(Mockito.anyString())
        val sdk = SourceAFISFingerprintSDKAdapter(40.0)

        val identity = generateIdentity(DID, query.imageByte)
        Mockito.doReturn(Flux.just(identity)).`when`(backend).search(any(), any(), any())

        val emptyMono: Mono<Any> = Mono.empty()
        Mockito.doReturn(emptyMono).`when`(bioAnalyzer).analyze(any(), any())

        var imageMatchingScore = 0.0
        val verificationEngine = VerificationEngine(backendManager, sdk, listOf("image/jpeg", "image/bmp", "image/png"), 20, checkReplayAttack, bioAnalyzer)
        StepVerifier.create(verificationEngine.match(query))
            .expectSubscription()
            .assertNext {
                Assert.assertThat(it.did, CoreMatchers.`is`(DID))
                imageMatchingScore = it.matchingScore
                Assert.assertTrue(imageMatchingScore >= 40.0)
            }
            .verifyComplete()

        val template = FingerprintTemplate(FingerprintImage().decode(query.imageByte)).serialize()
        var templateQuery = generateTemplateQuery(template)
        var templateMatchingScore = 0.0

        StepVerifier.create(verificationEngine.match(templateQuery))
            .expectSubscription()
            .assertNext {
                Assert.assertThat(it.did, CoreMatchers.`is`(DID))
                templateMatchingScore = it.matchingScore
                Assert.assertTrue(templateMatchingScore >= 40.0)
            }
            .verifyComplete()

        Assert.assertEquals("Image and its template matching score should be same", imageMatchingScore, templateMatchingScore, 0.0)
    }

    /**
     * Tests the match when the backend returns list of candidates.
     */
    @Test
    @Throws(Exception::class)
    fun testMatchAmongListOfCandidates() {
        Mockito.doReturn(backend).`when`(backendManager)!!.getbyName(Mockito.anyString())
        val sdk = SourceAFISFingerprintSDKAdapter(40.0)

        val identities = generateIdentities(10)
        identities.add(generateIdentity(DID, query.imageByte))

        Mockito.doReturn(Flux.fromIterable(identities)).`when`(backend)!!.search(any(), any(), any())
        Mockito.doNothing().`when`(backendManager).validateQuery(any())

        val emptyMono: Mono<Any> = Mono.empty()
        Mockito.doReturn(emptyMono).`when`(bioAnalyzer).analyze(any(), any())

        val verificationEngine = VerificationEngine(backendManager, sdk, listOf("image/jpeg", "image/bmp", "image/png"), 20, checkReplayAttack, bioAnalyzer)
        StepVerifier.create(verificationEngine.match(query))
            .expectSubscription()
            .assertNext {
                Assert.assertThat(it.did, CoreMatchers.`is`(DID))
                Assert.assertTrue(it.matchingScore >= 40.0)
            }
            .verifyComplete()
    }

    /**
     * Tests the match when the backend returns list of candidates. Running the matcher, there are multiple matches and
     * this tests conforms that the highest score match is returned. The matcher threshold is set to a small value so it
     * matches even very low quality fingerprints.
     */
    @Test
    @Throws(Exception::class)
    fun testMatchScoreSort() {
        Mockito.doReturn(backend).`when`(backendManager)!!.getbyName(Mockito.anyString())
        val sdk = SourceAFISFingerprintSDKAdapter(1.0)

        val lowQualityImageBytes = base64ToByte(loadBase64FromResource(LOW_QUALITY_IMAGE))
        val veryLowQualityImageBytes = base64ToByte(loadBase64FromResource(VERY_LOW_QUALITY_IMAGE))
        val identity1 = generateIdentity(DID, lowQualityImageBytes)
        val identity2 = generateIdentity(DID2, veryLowQualityImageBytes)
        val identity3 = generateIdentity(DID3, query.imageByte)
        val identity4 = generateIdentity(DID4, lowQualityImageBytes)

        Mockito.doReturn(Flux.just(identity1, identity2, identity3, identity4)).`when`(backend)!!.search(any(), any(), any())
        Mockito.doNothing().`when`(backendManager).validateQuery(any())

        val emptyMono: Mono<Any> = Mono.empty()
        Mockito.doReturn(emptyMono).`when`(bioAnalyzer).analyze(any(), any())

        val verificationEngine = VerificationEngine(backendManager, sdk, listOf("image/jpeg", "image/bmp", "image/png"), 20, checkReplayAttack, bioAnalyzer)
        StepVerifier.create(verificationEngine.match(query))
            .expectSubscription()
            .assertNext {
                Assert.assertThat("Invalid identity record returned", it.did, CoreMatchers.`is`(DID3))
                Assert.assertEquals("Invalid identity record returned", it.matchingScore, identity3.matchingScore, 0.0)
            }
            .verifyComplete()

        /**
         * The matching sdk should run over all identity records and therefore the matching score should be set for all
         * identities.
         */
        Assert.assertTrue("Invalid matching score returned", identity1.matchingScore >= 0.0)
        Assert.assertTrue("Invalid matching score returned", identity2.matchingScore >= 0.0)
        Assert.assertTrue("Invalid matching score returned", identity3.matchingScore >= 0.0)
        Assert.assertTrue("Invalid matching score returned", identity4.matchingScore >= 0.0)

        Assert.assertTrue("Sme fingerprints should have have same matching scores as well ", (identity1.matchingScore == identity4.matchingScore))
        Assert.assertTrue("Higher quality fingerprint should have higher matching score as well", identity1.matchingScore > identity2.matchingScore)

        Assert.assertTrue("Higher quality fingerprint should have higher matching score as well", identity3.matchingScore > identity1.matchingScore)
        Assert.assertTrue("Higher quality fingerprint should have higher matching score as well", identity3.matchingScore > identity2.matchingScore)
        Assert.assertTrue("Higher quality fingerprint should have higher matching score as well", identity3.matchingScore > identity4.matchingScore)
    }

    /** The sample did used in this test. */
    private val DID = "123"

    /** The sample did used in this test. */
    private val DID2 = "321"

    /** The sample did used in this test. */
    private val DID3 = "456"

    /** The sample did used in this test. */
    private val DID4 = "654"

    /** The sample text file. */
    private val TEXT1_FILE = "samplefile.txt"

    /** The sample low quality fingerprint image. */
    private val LOW_QUALITY_IMAGE = "images/fingerprint_low_quality.jpg"

    /** The sample very low quality fingerprint image. */
    private val VERY_LOW_QUALITY_IMAGE = "images/fingerprint_very_low_quality.jpg"

    /** The sample query used in this test. */
    private val query = generateQuery()

    /** The backend manager. */
    private val backendManager = Mockito.mock(IBackendManager::class.java)

    /** The Backend instance. */
    private val backend = Mockito.mock(IBackend::class.java)

    /** The CheckReplayAttack instance. */
    private val checkReplayAttack = Mockito.mock(ICheckReplayAttack::class.java)

    /** The BioAnalyzer instance. */
    private val bioAnalyzer = Mockito.mock(IBioAnalyzer::class.java)

    /** The EnvironmentVariable setter. */
    private val environmentsTest = EnvironmentsTest()
}
