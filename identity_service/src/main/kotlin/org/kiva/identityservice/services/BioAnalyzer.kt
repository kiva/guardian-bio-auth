package org.kiva.identityservice.services

import org.kiva.identityservice.config.EnvConfig
import org.kiva.identityservice.domain.BioanalyzerRequestData
import org.kiva.identityservice.errorhandling.exceptions.BioanalyzerServiceException
import org.kiva.identityservice.errorhandling.exceptions.api.FingerprintLowQualityException
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class BioAnalyzer(
    @param:Value("#{'\${namkha.valid_image_types}'.split(',')}")
    private val validImageTypes: List<String>,
    private val env: EnvConfig
) : IBioAnalyzer {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Calls bioanalyzer service to fetch the measured quality of submitted fingerprint, so, if the fingerprint quality is very low, the
     * corresponding error will be sent to client just in case the match not found could be because of very low quality of fingerprint.
     *
     * @param image the fingerprint image that must be analyzed.
     * @throws FingerprintLowQualityException when the fingerprint quality is less than defined threshold.
     * @throws InvalidImageFormatException when the submitted fingerprint image is not supported format.
     */
    override fun analyze(image: String, throwException: Boolean): Mono<Double> {

        try {
            if (env.bioanalyzerEnabled) {

                val reqId = MDC.get(REQUEST_ID)

                return WebClient.create(env.bioanalyzerServiceUrl)
                    .post()
                    .uri(ANALYZE_URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(REQUEST_ID_HEADER, reqId)
                    .bodyValue(mapOf(reqId to BioanalyzerRequestData("fingerprint", image)))
                    .exchangeToMono { response ->
                        if (response.statusCode().is2xxSuccessful) {
                            handleAnalyzerResponse(reqId, throwException, response)
                        } else {
                            logger.debug("Error happened in bioanalyzer call")
                            Mono.error(BioanalyzerServiceException())
                        }
                    }
            } else {
                return Mono.just(0.0)
            }
        } catch (e: Exception) {
            /**
             * Since the bioanalyzer error gets called when there is no match, we send the same exception just in case the bioanalyzer is down,
             * the score result is not parsable, etc.
             */
            logger.debug(e.message)
            return Mono.error(BioanalyzerServiceException(e.message))
        }
    }

    /**
     * Helper function that checks the bioanalyzer response status.
     *
     * @param requestID the requestIf the response belongs to.
     * @clientResponse the response received from bioanalyzer service.
     */
    private fun handleAnalyzerResponse(requestID: String, throwException: Boolean, clientResponse: ClientResponse): Mono<Double> {
        if (clientResponse.statusCode().is4xxClientError || clientResponse.statusCode().is5xxServerError) {
            logger.warn("The bioanalyzer returned " + clientResponse.statusCode())
            return Mono.error(BioanalyzerServiceException())
        }
        return clientResponse
            .bodyToMono(MutableMap::class.java)
            .flatMap { handleAnalyzerData(requestID, throwException, it as Map<String, Any>) }
    }

    /**
     * Helper function that parses bioanalyzer response data. If there is no error, it returns Mono.empty().
     *
     * @param requestID the requestIf the response belongs to.
     * @data the data received from bioanalyzer service.
     */
    private fun handleAnalyzerData(requestID: String, throwException: Boolean, data: Map<String, Any>): Mono<Double> {
        try {
            val result = data[requestID]
                ?.let { it as Map<String, Any> }
                ?: run { mapOf<String, Any>() }
            logger.info("Successful call to bioanalyzer $result")

            // let's ensure image format is what we want
            val imageFormat = result["format"]
            if (!validImageTypes.contains(imageFormat)) {
                val msg = "Invalid image format. Image format $imageFormat is not supported, must be one of ${validImageTypes.joinToString(", ")}"
                logger.warn(msg)
                return Mono.error(BioanalyzerServiceException(msg))
            }

            // let's ensure that quality is what we want however what is a good quality score for our case?
            // more reading + measuring necessary
            val fingerprintQuality = result["quality"]
                ?.let { it as Double }
                ?: run { -1.0 } // Quality is negative: autofail
            if (fingerprintQuality < env.bioanalyzerQualityThreshold && throwException) {
                val msg = "Low image quality! Min threshold is ${env.bioanalyzerQualityThreshold}, whereas computed quality is $fingerprintQuality"
                logger.warn(msg)
                return Mono.error(FingerprintLowQualityException(msg))
            }

            return Mono.just(fingerprintQuality)
        } catch (e: Exception) {
            logger.warn(e.message)
            return Mono.error(BioanalyzerServiceException(e.message))
        }
    }

    /** The bioanalyzer analyze endpoint url. */
    private val ANALYZE_URL: String = "/api/v1/analyze"

    /** The request id header name. */
    private val REQUEST_ID_HEADER: String = "x-request-id"

    /** The request id filed name. */
    private val REQUEST_ID: String = "reqid"
}
