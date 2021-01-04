package org.kiva.identityservice.services.backends.drivers

import org.kiva.identityservice.domain.DataType
import org.kiva.identityservice.domain.FingerPosition
import org.kiva.identityservice.domain.Identity
import org.kiva.identityservice.domain.VerifyRequest
import org.kiva.identityservice.errorhandling.exceptions.InvalidBackendDefinitionException
import org.kiva.identityservice.services.backends.Definition
import org.kiva.identityservice.services.backends.IBackend
import org.kiva.identityservice.services.backends.Operator
import org.kiva.identityservice.services.sdks.IBiometricSDKAdapter
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 * Driver for fetching identity records from an sql backend
 */
abstract class SqlBackend : IBackend {

    override var requiredFilters: MutableSet<String> = mutableSetOf()
    override var uniqueFilters: MutableSet<String> = mutableSetOf()
    override var listFilters: MutableSet<String> = mutableSetOf()
    override var hashedFilters: MutableSet<String> = mutableSetOf()
    override var filters: MutableSet<String> = mutableSetOf()
    override var validFingerPositions: MutableSet<FingerPosition> = mutableSetOf()
    override var filterMappers: MutableMap<String, Pair<String, Operator>> = mutableMapOf()

    protected val backendInitializeErrorMsg = "Error initializing the backend."

    /**
     * Should search backend fingerprint store and return prints that match the provided criteria
     *
     * @param verifyRequest the search query.
     * @param types the data types.
     * @param sdk the backend biometric matching service.
     */
    override fun search(verifyRequest: VerifyRequest, types: Array<DataType>, sdk: IBiometricSDKAdapter?): Flux<Identity> {
        return Flux.empty()
    }

    /**
     * Should search backend fingerprint store and return the positions of fingers for a given person stored in the
     * backend in the order of their quality
     *
     * @param filters the search matching filters.
     */
    override fun positions(filters: Map<String, String>): Flux<FingerPosition> {
        return Flux.empty()
    }

    /**
     * Helper function that should ping backend, testing for connectivity
     */
    override fun healthcheck(verifyRequest: VerifyRequest): Mono<Boolean> {
        return true.toMono()
    }

    /**
     * Enables backends to validate definitions, ensuring that options they need to work is provided
     */
    @Throws(InvalidBackendDefinitionException::class)
    override fun validateDefinition(definition: Definition) {
    }

    @Throws(InvalidBackendDefinitionException::class)
    override fun validateFilter(filter: Map<String, Any>) {
    }
}
