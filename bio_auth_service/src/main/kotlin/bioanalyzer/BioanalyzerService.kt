package org.kiva.bioauthservice.bioanalyzer

import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.kiva.bioauthservice.app.config.BioanalyzerConfig
import org.kiva.bioauthservice.bioanalyzer.dtos.BioanalyzerReponseDto
import org.kiva.bioauthservice.bioanalyzer.dtos.BioanalyzerRequestDto
import org.kiva.bioauthservice.common.errors.impl.BioanalyzerServiceException
import org.kiva.bioauthservice.common.errors.impl.FingerprintLowQualityException
import org.slf4j.Logger

@KtorExperimentalAPI
class BioanalyzerService(
    private val logger: Logger,
    private val bioanalyzerConfig: BioanalyzerConfig,
    private val httpClient: HttpClient
) {

    /**
     * Calls Bioanalyzer Service to fetch the measured quality of submitted fingerprint, so, if the fingerprint quality is very low, the
     * corresponding error will be sent to client.
     */
    suspend fun analyze(image: String, throwException: Boolean, requestId: String): Double {
        logger.debug("Analyzing image...")

        // If Bioanalyzer is not enabled, return early and skip the call to Bioanalyzer Service
        if (!bioanalyzerConfig.enabled) {
            logger.debug("Bioanalyzer Service is disabled, aborting call to analyze image.")
            return 0.0
        }

        // Send request to Bioanalyzer Service
        val response: HttpResponse = try {
            httpClient.post(bioanalyzerConfig.baseUrl + bioanalyzerConfig.analyzePath) {
                contentType(ContentType.Application.Json)
                body = mapOf(requestId to BioanalyzerRequestDto("fingerprint", image))
                headers {
                    append(HttpHeaders.XRequestId, requestId)
                }
            }
        } catch (e: Exception) {
            // Since the bioanalyzer error gets called when there is no match, we send the same exception just in case the bioanalyzer is
            // down, the score result is not parsable, etc.
            logger.debug(e.message)
            throw BioanalyzerServiceException(e.message)
        }

        // Handle response from Bioanalyzer Service
        if (response.status.isSuccess()) {

            // get response body
            val responseBody = try {
                val responseText = response.readText()
                logger.debug("Successful call to bioanalyzer $responseText")
                Json.decodeFromString<Map<String, BioanalyzerReponseDto>>(responseText)
            } catch (e: Exception) {
                logger.debug("Could not parse response from Bioanalyzer")
                throw BioanalyzerServiceException("Could not parse response from Bioanalyzer")
            }

            // let's ensure that quality is what we want (if not provided, autofail by setting quality to a negative value)
            val fingerprintQuality = responseBody[requestId]?.quality ?: -1.0
            if (fingerprintQuality < bioanalyzerConfig.qualityThreshold) {
                val msg =
                    "Low image quality! Min threshold is ${bioanalyzerConfig.qualityThreshold}, whereas computed quality is $fingerprintQuality"
                logger.warn(msg)
                if (throwException) throw FingerprintLowQualityException(msg)
            }

            // return quality
            return fingerprintQuality
        } else {
            logger.warn("The bioanalyzer returned status code ${response.status.value}")
            throw BioanalyzerServiceException()
        }
    }
}
