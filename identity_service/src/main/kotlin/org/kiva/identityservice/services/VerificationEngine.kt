package org.kiva.identityservice.services

import java.time.Duration
import java.util.concurrent.TimeoutException
import org.jnbis.api.Jnbis
import org.kiva.identityservice.domain.DataType
import org.kiva.identityservice.domain.Identity
import org.kiva.identityservice.domain.Query
import org.kiva.identityservice.errorhandling.exceptions.BioanalyzerServiceException
import org.kiva.identityservice.errorhandling.exceptions.FingerPrintTemplateException
import org.kiva.identityservice.errorhandling.exceptions.InvalidBackendException
import org.kiva.identityservice.errorhandling.exceptions.api.ApiExceptionCode
import org.kiva.identityservice.errorhandling.exceptions.api.FingerprintLowQualityException
import org.kiva.identityservice.errorhandling.exceptions.api.FingerprintMissingNotCapturedException
import org.kiva.identityservice.errorhandling.exceptions.api.FingerprintNoMatchException
import org.kiva.identityservice.errorhandling.exceptions.api.InvalidImageFormatException
import org.kiva.identityservice.errorhandling.exceptions.api.InvalidQueryFilterException
import org.kiva.identityservice.errorhandling.exceptions.api.NoCitizenFoundException
import org.kiva.identityservice.services.backends.IBackend
import org.kiva.identityservice.services.backends.IBackendManager
import org.kiva.identityservice.services.sdks.IBiometricSDKAdapter
import org.kiva.identityservice.utils.detectContentType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class VerificationEngine(
    private val backendManager: IBackendManager,
    private val sdk: IBiometricSDKAdapter,
    @param:Value("#{'\${namkha.valid_image_types}'.split(',')}")
    private val validImageTypes: List<String>,
    @param:Value("\${namkha.backend.fetch_limit}")
    private val fetchLimit: Long,
    private val checkReplayAttack: ICheckReplayAttack,
    private val bioAnalyzer: IBioAnalyzer
) : IVerificationEngine {

    override fun match(query: Query): Mono<Identity> {
        try {
            // let's check that image is of a supported type
            var contentType = detectContentType(query.imageByte)
            if (query.imageType == DataType.IMAGE) {

                // Tika parser detects wsq as octet-stream, so, it is converted to png and match is called using its png version.
                if (contentType == OCTET_STREAM_CONTENT_TYPE) {
                    contentType = detectContentType(Jnbis.wsq().decode(query.imageByte).toPng().asByteArray())
                }

                if (!validImageTypes.contains(contentType)) {
                    val e = InvalidImageFormatException(ApiExceptionCode.INVALID_IMAGE_FORMAT.msg)
                    logErrorMessage(e)
                    return Mono.error(e)
                }
            } else if (query.imageType == DataType.TEMPLATE) {
                // For template image queries, the image field should be plain text of json template.
                if (contentType != PLAIN_TEXT_CONTENT_TYPE) {
                    val e = InvalidImageFormatException(ApiExceptionCode.INVALID_IMAGE_FORMAT.msg)
                    logErrorMessage(e)
                    return Mono.error(e)
                }
            }

            // let's validate query filter
            backendManager.validateQuery(query)

            // compute hash & check against database
            checkReplayAttack.isReplayAttack(query)

            // let's get backend
            val backend: IBackend = backendManager.getbyName(query.backend)

            return sdk.match(query, fetchCandidates(backend, query, DataType.values()))
                    .onErrorResume(FingerPrintTemplateException::class.java) {
                        // we need to redo with only image if template fails for some reason
                        val newCandidates = fetchCandidates(backend, query, arrayOf(DataType.IMAGE))
                        sdk.match(query, newCandidates) }
                    // Since no match found, let's run bio-analyzer service, just in case it might be because of fingerprint's low quality.
                    // exception will be thrown back to consumer if quality is too low
                    .switchIfEmpty(bioAnalyzer.analyze(query.image, true).timeout(BIOANALYZER_TIMEOUT).flatMap { Mono.empty<Identity>() })
                    // If the bioanalyzer did not give us the low quality error, let's return FingerprintNoMatchException.
                    .switchIfEmpty(Mono.error(FingerprintNoMatchException()))
                    .last() // Return the highest matching score candidate
                    .doOnError {
                        logErrorMessage(it)
                    }
                    .onErrorResume(BioanalyzerServiceException::class.java) {
                        Mono.error(FingerprintNoMatchException())
                    }
                    .onErrorResume(TimeoutException::class.java) {
                        Mono.error(FingerprintNoMatchException())
                    }
        } catch (e: InvalidQueryFilterException) {
            logErrorMessage(e)
            return Mono.error(e)
        } catch (e: InvalidBackendException) {
            logErrorMessage(e)
            return Mono.error(e)
        } catch (e: InvalidImageFormatException) {
            logErrorMessage(e)
            return Mono.error(e)
        } catch (e: Exception) {
            logErrorMessage(e)
            return Mono.error(e)
        }
    }

    private fun fetchCandidates(backend: IBackend, query: Query, types: Array<DataType>): Flux<Identity> {

        // we want to search with template and if not available image.
        return backend.search(query, types, sdk)
                // let's return no citizens found exception if results come up empty
                .switchIfEmpty(Flux.error(NoCitizenFoundException()))
                // consider only people with the position we want
                .filter { it.fingerprints.keys.contains(query.position) }
                // we don't have prints for that position so let's throw the pertinent error message.
                .switchIfEmpty(Flux.error(FingerprintMissingNotCapturedException()))
                // let's force backend to be clever by limiting returned
                .limitRequest(fetchLimit)
                .take(fetchLimit)
    }

    /**
     * Helper function logs the error message corresponds to the error.
     *
     * @param error the error exception.
     */
    private fun logErrorMessage(error: Throwable?) {
        if (error is NoCitizenFoundException) {
            logger.warn(ApiExceptionCode.NO_CITIZEN_FOUND.msg, error)
        } else if (error is FingerprintMissingNotCapturedException) {
            logger.warn(ApiExceptionCode.FINGERPRINT_MISSING_NOT_CAPTURED.msg, error)
        } else if (error is FingerprintNoMatchException) {
            logger.warn(ApiExceptionCode.FINGERPRINT_NO_MATCH.msg, error)
        } else if (error is InvalidBackendException) {
            logger.error(ApiExceptionCode.INVALID_BACKEND_NAME.msg, error)
        } else if (error is FingerprintLowQualityException) {
            logger.warn(ApiExceptionCode.FINGERPRINT_LOW_QUALITY.msg, error)
        } else if (error is InvalidQueryFilterException) {
            logger.warn(ApiExceptionCode.INVALID_FILTERS.msg, error)
        } else if (error is FingerPrintTemplateException) {
            logger.error(ApiExceptionCode.INVALID_TEMPLATE_VERSION.msg, error)
        } else if (error is InvalidImageFormatException) {
            logger.error(ApiExceptionCode.INVALID_IMAGE_FORMAT.msg, error)
        } else if (error is TimeoutException) {
            logger.error(BIOANALYZER_TIMEOUT_MESSAGE, error)
        } else {
            logger.error(error?.message)
        }
    }

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /** The octet stream content type. */
    private val OCTET_STREAM_CONTENT_TYPE = "application/octet-stream"

    /** The plain text content type. */
    private val PLAIN_TEXT_CONTENT_TYPE = "text/plain"

    /** The timeout for bioanalyzer service call. */
    private val BIOANALYZER_TIMEOUT: Duration = Duration.ofSeconds(5L)

    /** The bioanalyzer service timeout's error message. */
    private val BIOANALYZER_TIMEOUT_MESSAGE = "Bioanalyzer did not return answer in the specified time."
}
