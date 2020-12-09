package org.kiva.identityservice.services.backends

import com.machinezoo.sourceafis.FingerprintTemplate
import org.kiva.identityservice.domain.Fingerprint
import org.kiva.identityservice.domain.StoreRequest
import org.kiva.identityservice.errorhandling.exceptions.FingerprintTemplateGenerationException
import org.kiva.identityservice.errorhandling.exceptions.InvalidBackendOperationException
import org.kiva.identityservice.services.IBioAnalyzer
import org.kiva.identityservice.services.sdks.IBiometricSDKAdapter
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface IHasTemplateSupport {

    /**
     * Generates the template for given list of fingerprints and updates the backend for those records.
     *
     * @param records the records that template generation is performed on.
     * @param sdk the backend biometric matching service defines the template version and type.
     * @param bioAnalyzer the bio analyzer service instance.
     * @return list of results per each given record's template generation operation.
     *
     * @throws FingerprintTemplateGenerationException if error happens in template generation.
     * @throws InvalidBackendOperationException if the backend does not support bulk template generation.
     */
    @Throws(FingerprintTemplateGenerationException::class, InvalidBackendOperationException::class)
    fun templateGenerate(records: Flux<Fingerprint>, throwException: Boolean, sdk: IBiometricSDKAdapter, bioAnalyzer: IBioAnalyzer): Flux<Int>

    /**
     * Provided a fingerprint template, store it in the appropriate backend.
     *
     * @param sdk the backend biometric matching service defines the template version and type.
     * @param storeRequest the details of the fingerprint template to be stored
     */
    fun storeTemplate(sdk: IBiometricSDKAdapter, storeRequest: StoreRequest): Mono<FingerprintTemplate>
}
