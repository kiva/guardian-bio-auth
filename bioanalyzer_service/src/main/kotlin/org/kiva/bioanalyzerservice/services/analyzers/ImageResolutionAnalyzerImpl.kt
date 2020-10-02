package org.kiva.bioanalyzerservice.services.analyzers

import org.kiva.bioanalyzerservice.domain.AnalysisType
import org.kiva.bioanalyzerservice.domain.BioType
import reactor.core.publisher.Mono

class ImageResolutionAnalyzerImpl : BioDataAnalyzer {

    override fun type(): AnalysisType = AnalysisType.RESOLUTION

    override fun supported(): List<BioType> = listOf(BioType.FINGERPRINT)

    override fun analyze(data: ByteArray, bioType: BioType, format: String?): Mono<Any> {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}
