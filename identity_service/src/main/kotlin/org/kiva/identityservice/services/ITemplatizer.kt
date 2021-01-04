package org.kiva.identityservice.services

import org.kiva.identityservice.domain.Fingerprint
import org.kiva.identityservice.domain.SaveRequest
import org.kiva.identityservice.services.backends.IHasTemplateSupport
import reactor.core.publisher.Mono

/**
 * Generates templates for a backend
 */
interface ITemplatizer {

    fun bulkGenerate(throwException: Boolean, backend: IHasTemplateSupport, records: List<Fingerprint>): Mono<Long>

    fun bulkSave(backend: IHasTemplateSupport, saveRequests: List<SaveRequest>): Mono<Long>
}
