package org.kiva.identityservice.domain

import javax.validation.constraints.Size

data class BulkSaveRequest(
    @Size(min = 1, max = 1000)
    val fingerprints: List<SaveRequest>
)
