package org.kiva.bioanalyzerservice.services

import org.kiva.bioanalyzerservice.domain.AnalysisType
import org.kiva.bioanalyzerservice.domain.BioType
import org.kiva.bioanalyzerservice.services.analyzers.BioDataAnalyzer
import org.kiva.bioanalyzerservice.services.analyzers.ImageFormatAnalyzer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

@Service
class BioDataAnalysisEngineImpl(private val analysers: List<BioDataAnalyzer>) : BioDataAnalysisEngine {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val imageFormatAnalyzer = analysers.filterIsInstance<ImageFormatAnalyzer>().first()

    override fun execute(bioData: ByteArray, bioType: BioType, analysis: List<AnalysisType>): Mono<Map<AnalysisType, Any>> {
        return imageFormatAnalyzer.analyze(bioData, bioType)
            .flatMap { format ->
                analysers
                    .filter {
                        it !is ImageFormatAnalyzer &&
                            it.supported().contains(bioType) &&
                            analysis.contains(it.type())
                    } // let's ensure that analyzer supports
                    .toFlux()
                    .flatMap { it.analyze(bioData, bioType, format as String).map { res -> it.type() to res } }
                    .collectMap(Pair<AnalysisType, Any>::first, Pair<AnalysisType, Any>::second)
                    .doOnSuccess { it[imageFormatAnalyzer.type()] = format }
                    .doOnNext { logger.info("Bio Analysis Result -  $it") }
            }
    }
}
