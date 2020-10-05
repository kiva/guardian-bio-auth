package org.kiva.bioanalyzerservice.services.analyzers

import org.kiva.bioanalyzerservice.domain.AnalysisType
import org.kiva.bioanalyzerservice.domain.BioType
import reactor.core.publisher.Mono

/**
 * Algorithms run against bio images analyzing them
 */
interface BioDataAnalyzer {

    /**
     * Type of analysis that this analyser runs. New analysis types should extend
     * @see AnalysisType
     */
    fun type(): AnalysisType

    /**
     * Bio data type being supported.
     */
    fun supported(): List<BioType>

    /**
     * Does the difficult job of analyzing the image
     */
    fun analyze(data: ByteArray, bioType: BioType, format: String? = null): Mono<Any>
}
