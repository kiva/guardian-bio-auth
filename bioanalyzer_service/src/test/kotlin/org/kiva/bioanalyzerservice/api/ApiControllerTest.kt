package org.kiva.bioanalyzerservice.api

import org.junit.Test
import org.junit.runner.RunWith
import org.kiva.bioanalyzerservice.domain.AnalysisType
import org.kiva.bioanalyzerservice.domain.BioType
import org.kiva.bioanalyzerservice.services.analyzers.NFIQ2FingerPrintQualityChecker
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.StreamUtils
import reactor.core.publisher.Mono
import java.util.Base64
import java.util.concurrent.TimeoutException

@SpringBootTest
@RunWith(SpringRunner::class)
@AutoConfigureWebTestClient
class ApiControllerTest() {

    @MockBean
    lateinit var analysisEngine: NFIQ2FingerPrintQualityChecker

    @Autowired
    lateinit var webTestClient: WebTestClient

    /** The sample jpeg fingerprint image. */
    private val jpgFingerprint = "images/fingerprint.jpg"

    /** The sample wsq fingerprint image. */
    private val wsqFingerprint = "images/sample_fingerprint.wsq"

    /** The endpoint url address. */
    private val endpointBaseUrl: String = "/api/v1/"

    /**
     * Tests the healthz endpoint.
     */
    @Test
    fun verifyHealth() {
        webTestClient.get().uri("$endpointBaseUrl/healthz")
                .exchange()
                .expectStatus().isOk
                .expectBodyList(String::class.java)
                .hasSize(1)
                .contains("OK")
    }

    /**
     * Tests the analyze endpoint for jpeg image.
     */
    @Test
    fun analyzeJpg() {

        val imageBase64: String = Base64.getEncoder().encodeToString(
                StreamUtils.copyToByteArray(ClassPathResource(jpgFingerprint).inputStream))
        val query = Query(imageBase64, BioType.FINGERPRINT)

        Mockito.doReturn(Mono.just(50F)).`when`(analysisEngine).analyze(query.imageByte, BioType.FINGERPRINT, "image/jpeg")
        Mockito.doReturn(listOf(BioType.FINGERPRINT)).`when`(analysisEngine).supported()
        Mockito.doReturn(AnalysisType.QUALITY).`when`(analysisEngine).type()

        webTestClient.post().uri("$endpointBaseUrl/analyze")
                .syncBody(mapOf("key" to query))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .json("{\"key\":{\"format\":\"image/jpeg\",\"quality\":50.0}}")
    }

    /**
     * Tests the analyze endpoint for wsq image.
     */
    @Test
    fun analyzeWsq() {

        val imageBase64: String = Base64.getEncoder().encodeToString(
                StreamUtils.copyToByteArray(ClassPathResource(wsqFingerprint).inputStream))
        val query = Query(imageBase64, BioType.FINGERPRINT)

        Mockito.doReturn(Mono.just(40F)).`when`(analysisEngine).analyze(query.imageByte, BioType.FINGERPRINT, "image/wsq")

        Mockito.doReturn(listOf(BioType.FINGERPRINT)).`when`(analysisEngine).supported()
        Mockito.doReturn(AnalysisType.QUALITY).`when`(analysisEngine).type()

        webTestClient.post().uri("$endpointBaseUrl/analyze")
                .syncBody(mapOf("key" to query))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .json("{\"key\":{\"format\":\"image/wsq\",\"quality\":40.0}}")
    }

    /**
     * Tests the analyze endpoint for analyzing multiple images with different formats.
     */
    @Test
    fun analyzeImages() {

        val image1Base64: String = Base64.getEncoder().encodeToString(
                StreamUtils.copyToByteArray(ClassPathResource(jpgFingerprint).inputStream))
        val query1 = Query(image1Base64, BioType.FINGERPRINT)
        Mockito.doReturn(Mono.just(40F)).`when`(analysisEngine).analyze(query1.imageByte, BioType.FINGERPRINT, "image/jpeg")

        val image2Base64: String = Base64.getEncoder().encodeToString(
                StreamUtils.copyToByteArray(ClassPathResource(wsqFingerprint).inputStream))
        val query2 = Query(image2Base64, BioType.FINGERPRINT)
        Mockito.doReturn(Mono.just(50F)).`when`(analysisEngine).analyze(query2.imageByte, BioType.FINGERPRINT, "image/wsq")

        Mockito.doReturn(listOf(BioType.FINGERPRINT)).`when`(analysisEngine).supported()
        Mockito.doReturn(AnalysisType.QUALITY).`when`(analysisEngine).type()

        webTestClient.post().uri("$endpointBaseUrl/analyze")
                .syncBody(mapOf("key1" to query1, "key2" to query2))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .json("{\"key1\":{\"format\":\"image/jpeg\",\"quality\":40.0},\"key2\":{\"format\":\"image/wsq\",\"quality\":50.0}}")
    }

    /**
     * Negative test for the case when the quality analyzer is not supported.
     */
    @Test
    fun analyzeWhenBioTypeNotSupported() {

        val imageBase64: String = Base64.getEncoder().encodeToString(
                StreamUtils.copyToByteArray(ClassPathResource(jpgFingerprint).inputStream))
        val query = Query(imageBase64, BioType.FINGERPRINT)

        Mockito.doReturn(Mono.just(50F)).`when`(analysisEngine).analyze(query.imageByte, BioType.FINGERPRINT, "image/jpeg")
        Mockito.doReturn(emptyList<BioType>()).`when`(analysisEngine).supported()

        webTestClient.post().uri("$endpointBaseUrl/analyze")
                .syncBody(mapOf("key" to query))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .jsonPath("$.key.format")
                .exists()
                .jsonPath("$.key.quality")
                .doesNotExist()
    }

    /**
     * Negative test for the case when the quality analyzer returns TimeoutException error.
     */
    @Test
    fun analyzeNfiqTimeout() {

        val imageBase64: String = Base64.getEncoder().encodeToString(
                StreamUtils.copyToByteArray(ClassPathResource(jpgFingerprint).inputStream))
        val query = Query(imageBase64, BioType.FINGERPRINT)

        Mockito.doReturn(Mono.error<Any>(TimeoutException())).`when`(analysisEngine).analyze(query.imageByte, BioType.FINGERPRINT, "image/jpeg")
        Mockito.doReturn(listOf(BioType.FINGERPRINT)).`when`(analysisEngine).supported()
        Mockito.doReturn(AnalysisType.QUALITY).`when`(analysisEngine).type()

        webTestClient.post().uri("$endpointBaseUrl/analyze")
                .syncBody(mapOf("key" to query))
                .exchange()
                .expectStatus().is5xxServerError
    }
}