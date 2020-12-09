package org.kiva.identityservice.services

import com.machinezoo.sourceafis.FingerprintTemplate
import org.kiva.identityservice.domain.Fingerprint
import org.kiva.identityservice.domain.StoreRequest
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

    override fun store(backend: IHasTemplateSupport, storeRequest: StoreRequest): Mono<FingerprintTemplate> {
        return backend.storeTemplate(sdkAdapter, storeRequest)
    }
}
