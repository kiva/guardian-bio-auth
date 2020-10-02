package org.kiva.bioanalyzerservice.services

import org.kiva.bioanalyzerservice.domain.AnalysisType
import org.kiva.bioanalyzerservice.domain.BioType
import reactor.core.publisher.Mono

/**
 * Engine driving analysis. Gathers declared analyzers and calls each
 */
interface BioDataAnalysisEngine {

    fun execute(bioData: ByteArray, bioType: BioType, analysis: List<AnalysisType>): Mono<Map<AnalysisType, Any>>
}
