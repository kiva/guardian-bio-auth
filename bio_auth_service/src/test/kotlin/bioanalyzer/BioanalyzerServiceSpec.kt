package bioanalyzer

import BioAnalyzerRoute
import alphanumericStringGen
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.arbitrary.next
import io.ktor.http.HttpStatusCode
import io.ktor.util.KtorExperimentalAPI
import io.mockk.every
import io.mockk.mockk
import mockHttpClient
import org.kiva.bioauthservice.app.config.BioanalyzerConfig
import org.kiva.bioauthservice.bioanalyzer.BioanalyzerService
import org.kiva.bioauthservice.bioanalyzer.dtos.BioanalyzerReponseDto
import org.kiva.bioauthservice.common.errors.impl.BioanalyzerServiceException
import org.kiva.bioauthservice.common.errors.impl.FingerprintLowQualityException
import org.kiva.bioauthservice.common.utils.toBase64String
import org.slf4j.LoggerFactory

@KtorExperimentalAPI
class BioanalyzerServiceSpec : StringSpec({

    val logger = LoggerFactory.getLogger(this.javaClass)
    val config = mockk<BioanalyzerConfig>()
    val baseUrl = "http://bioanalyzer-service:8080"
    val analyzePath = "/api/v1/analyze"
    val bioanalyzerUrl = baseUrl + analyzePath
    val image = this.javaClass.getResource("/images/sample.png")?.readBytes()?.toBase64String() ?: ""
    val requestId = alphanumericStringGen.next()

    beforeEach {
        every { config.enabled } returns true
        every { config.qualityThreshold } returns 40.0
        every { config.baseUrl } returns baseUrl
        every { config.analyzePath } returns analyzePath
    }

    "should be able to analyze an image" {
        val qualityScore = 99.0
        val route = BioAnalyzerRoute(bioanalyzerUrl, BioanalyzerReponseDto(qualityScore))
        val httpClient = mockHttpClient(route)
        val bioanalyzerService = BioanalyzerService(logger, config, httpClient)
        val result = bioanalyzerService.analyze(image, false, requestId)
        result shouldBe qualityScore
    }

    "should not analyze an image if disabled" {
        val route = BioAnalyzerRoute(bioanalyzerUrl, null, HttpStatusCode.InternalServerError, "Oops!")
        val httpClient = mockHttpClient(route)
        every { config.enabled } returns false
        val bioanalyzerService = BioanalyzerService(logger, config, httpClient)
        val result = bioanalyzerService.analyze(image, false, requestId)
        result shouldBe 0.0
    }

    "should return -1.0 if the Bioanalyzer fails to return a quality score" {
        val route = BioAnalyzerRoute(bioanalyzerUrl, BioanalyzerReponseDto(null))
        val httpClient = mockHttpClient(route)
        val bioanalyzerService = BioanalyzerService(logger, config, httpClient)
        val result = bioanalyzerService.analyze(image, false, requestId)
        result shouldBe -1.0
    }

    "should not throw an error if an image has low quality, but throwException is false" {
        val qualityScore = 1.0
        val route = BioAnalyzerRoute(bioanalyzerUrl, BioanalyzerReponseDto(qualityScore))
        val httpClient = mockHttpClient(route)
        val bioanalyzerService = BioanalyzerService(logger, config, httpClient)
        val result = bioanalyzerService.analyze(image, false, requestId)
        result shouldBe qualityScore
    }

    "should throw an error if an image has low quality and throwException is true" {
        val qualityScore = 1.0
        val route = BioAnalyzerRoute(bioanalyzerUrl, BioanalyzerReponseDto(qualityScore))
        val httpClient = mockHttpClient(route)
        val bioanalyzerService = BioanalyzerService(logger, config, httpClient)
        shouldThrow<FingerprintLowQualityException> {
            bioanalyzerService.analyze(image, true, requestId)
        }
    }

    "should throw an error if the target route is wrong" {
        val route = BioAnalyzerRoute("$baseUrl/foo", BioanalyzerReponseDto(99.0))
        val httpClient = mockHttpClient(route)
        val bioanalyzerService = BioanalyzerService(logger, config, httpClient)
        shouldThrow<BioanalyzerServiceException> {
            bioanalyzerService.analyze(image, false, requestId)
        }
    }

    "should throw an error if the remote Bioanalyzer Service has an error" {
        val route = BioAnalyzerRoute(bioanalyzerUrl, null, HttpStatusCode.InternalServerError, "Oops!")
        val httpClient = mockHttpClient(route)
        val bioanalyzerService = BioanalyzerService(logger, config, httpClient)
        shouldThrow<BioanalyzerServiceException> {
            bioanalyzerService.analyze(image, false, requestId)
        }
    }

    "should throw an error if the remote Bioanalyzer Service returns a meaningless response" {
        val route = BioAnalyzerRoute(bioanalyzerUrl, null, HttpStatusCode.OK, "Oops!")
        val httpClient = mockHttpClient(route)
        val bioanalyzerService = BioanalyzerService(logger, config, httpClient)
        shouldThrow<BioanalyzerServiceException> {
            bioanalyzerService.analyze(image, false, requestId)
        }
    }
})
