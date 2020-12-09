package org.kiva.identityservice.services

import com.machinezoo.sourceafis.FingerprintTemplate
import org.kiva.identityservice.domain.Fingerprint
import org.kiva.identityservice.domain.StoreRequest
import org.kiva.identityservice.services.backends.IHasTemplateSupport
import reactor.core.publisher.Mono

/**
 * Generates templates for a backend
 */
interface ITemplatizer {

    fun bulkGenerate(throwException: Boolean, backend: IHasTemplateSupport, records: List<Fingerprint>): Mono<Long>

    fun store(backend: IHasTemplateSupport, storeRequest: StoreRequest): Mono<FingerprintTemplate>
}
