package org.kiva.identityservice.services

import org.kiva.identityservice.domain.Fingerprint
import org.kiva.identityservice.domain.SaveRequest
import org.kiva.identityservice.services.backends.IHasTemplateSupport
import org.kiva.identityservice.services.sdks.IBiometricSDKAdapter
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class Templatizer(
    private val sdkAdapter: IBiometricSDKAdapter,
    private val bioAnalyzer: IBioAnalyzer
) : ITemplatizer {

    override fun bulkGenerate(
        throwException: Boolean,
        backend: IHasTemplateSupport,
        records: List<Fingerprint>
    ): Mono<Long> {
        return backend.templateGenerate(Flux.fromIterable(records), throwException, sdkAdapter, bioAnalyzer).count()
    }

    override fun bulkSave(backend: IHasTemplateSupport, saveRequests: List<SaveRequest>): Mono<Long> {
        return backend.saveTemplates(sdkAdapter, Flux.fromIterable(saveRequests))
    }
}
