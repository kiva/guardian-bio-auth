package org.kiva.identityservice.services.sdks.sourceafis

import com.machinezoo.sourceafis.FingerprintCompatibility
import com.machinezoo.sourceafis.FingerprintImage
import com.machinezoo.sourceafis.FingerprintMatcher
import com.machinezoo.sourceafis.FingerprintTemplate
import org.kiva.identityservice.domain.DataType
import org.kiva.identityservice.domain.Identity
import org.kiva.identityservice.domain.VerifyRequest
import org.kiva.identityservice.errorhandling.exceptions.FingerPrintTemplateException
import org.kiva.identityservice.errorhandling.exceptions.api.ApiExceptionCode
import org.kiva.identityservice.services.sdks.IFingerprintSDKAdapter
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.util.Loggers
import java.io.ByteArrayInputStream
import java.io.DataInputStream

@Service
class SourceAFISFingerprintSDKAdapter(
    @param:Value("\${namkha.sourceafis.match_threshold}")
    private val matchThreshold: Double
) : IFingerprintSDKAdapter {
    private val logger = Loggers.getLogger(javaClass)

    override val version: Int = 3
    override val templateType: String = "sourceafis"

    /**
     * among a list of candidate records check if probe matches any
     *
     * @return list of matching candidates
     */
    override fun match(verifyRequest: VerifyRequest, people: Flux<Identity>): Flux<Identity> {

        try {
            val queryTemplate = if (verifyRequest.imageType == DataType.IMAGE) {
                FingerprintTemplate(FingerprintImage().decode(verifyRequest.imageByte))
            } else {
                FingerprintTemplate().deserialize(String(verifyRequest.imageByte, Charsets.UTF_8))
            }
            val matcher = FingerprintMatcher().index(queryTemplate)

            return people
                .onErrorStop()
                .parallel()
                .runOn(Schedulers.parallel())
                .filter {
                    val template = when (it.type) {
                        DataType.IMAGE -> FingerprintTemplate(FingerprintImage().decode(it.fingerprints[verifyRequest.params.position]))
                        DataType.TEMPLATE -> {
                            // let's ensure we got sent in a version that works for us and that out backend isn't doing
                            // something stupid. We won't refuse to process, but just scream in the line below :)
                            if (it.templateVersion == null) {
                                logger.error("Template version not specified for '${it.national_id}' in ${verifyRequest.backend}")
                            }

                            // we check for exact equality here; other SDKs are free to check for version range
                            it.templateVersion?.let { check ->
                                if (check != version)
                                    throw FingerPrintTemplateException(ApiExceptionCode.INVALID_TEMPLATE_VERSION.msg)
                            }
                            FingerprintTemplate().deserialize(String(it.fingerprints[verifyRequest.params.position]!!, Charsets.UTF_8))
                        }
                    }
                    it.matchingScore = matcher.match(template)
                    val match = it.matchingScore >= matchThreshold

                    logger.debug("SourceAFISFingerprintSDKAdapter identity match for : $it \n Score: ${it.matchingScore} \n Match : $match")
                    match
                }
                .sequential()
                .sort(
                    fun(o1: Identity, o2: Identity): Int { // Sort the list of matches from lowest matching score to highest.
                        return o1.matchingScore.compareTo(o2.matchingScore)
                    }
                )
        } catch (e: Exception) {
            return Flux.error(e)
        }
    }

    private fun isForeignTemplate(template: ByteArray): Boolean {
        val input = DataInputStream(ByteArrayInputStream(template))
        return input.readByte() == 'F'.toByte() && input.readByte() == 'M'.toByte() && input.readByte() == 'R'.toByte()
    }

    override fun buildTemplate(template: ByteArray): Mono<FingerprintTemplate> {
        return Mono.fromCallable {
            if (isForeignTemplate(template)) {
                FingerprintCompatibility.convert(template)
            } else {
                FingerprintTemplate(template)
            }
        }
    }

    override fun buildTemplateFromImage(image: ByteArray): Mono<Any> =
        Mono.fromCallable { FingerprintTemplate(FingerprintImage().decode(image)).serialize() }
}
