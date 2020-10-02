package org.kiva.bioanalyzerservice.services.analyzers

import org.apache.tika.Tika
import org.jnbis.api.Jnbis
import org.kiva.bioanalyzerservice.domain.AnalysisType
import org.kiva.bioanalyzerservice.domain.BioType
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Detects and returns the format of the image
 * NOTE: order annotation is important as other analyzers might depend on format of the image
 */
@Service
@Order(-100)
class ImageFormatAnalyzer : BioDataAnalyzer {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun type(): AnalysisType = AnalysisType.FORMAT

    override fun supported(): List<BioType> = listOf(BioType.FINGERPRINT)

    override fun analyze(data: ByteArray, bioType: BioType, format: String?): Mono<Any> = Mono.fromCallable {
        var imgFormat = Tika().detect(data)
        if (imgFormat == "application/octet-stream") { // tika does not handle wsq
            try {
                Jnbis.wsq().decode(data)
                imgFormat = "image/wsq"
            } catch (e: Exception) {
                logger.error("dealing with unknown format", e)
            }
        }
        imgFormat
    }
}
