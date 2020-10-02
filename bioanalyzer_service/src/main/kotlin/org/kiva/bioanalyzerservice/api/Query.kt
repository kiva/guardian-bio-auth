package org.kiva.bioanalyzerservice.api

import org.kiva.bioanalyzerservice.domain.AnalysisType
import org.kiva.bioanalyzerservice.domain.BioType
import java.util.Base64
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

/**
 * Query of images for which we want to check quality scores
 */
data class Query(

    /**
     * Base64 representation of the bio data e.g.
     */
    @NotBlank
    val image: String,

    /**
     * Type of bio data being analyzed
     */
    @NotNull
    val type: BioType,

    /**
     * Allows clients to specify what analysis the want run
     */
    @NotEmpty
    val analysis: List<AnalysisType> = AnalysisType.values().toList()
) {
    var imageByte: ByteArray = Base64.getDecoder().decode(image)
}
