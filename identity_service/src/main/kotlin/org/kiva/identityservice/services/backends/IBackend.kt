package org.kiva.identityservice.services.backends

import org.kiva.identityservice.domain.DataType
import org.kiva.identityservice.domain.FingerPosition
import org.kiva.identityservice.domain.Identity
import org.kiva.identityservice.domain.Query
import org.kiva.identityservice.errorhandling.exceptions.InvalidBackendDefinitionException
import org.kiva.identityservice.services.sdks.IBiometricSDKAdapter
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Fetches identity data from a backend server. An example implementation will fetch identity data from the issuer.
 */
interface IBackend {

    val requiredFilters: MutableSet<String>
    val uniqueFilters: MutableSet<String>
    val listFilters: MutableSet<String>
    val hashedFilters: MutableSet<String>
    val filters: MutableSet<String>
    val validFingerPositions: MutableSet<FingerPosition>
    val filterMappers: MutableMap<String, Pair<String, Operator>>

    /**
     * initialization tasks required by backend can be thrown in here
     *
     * @throws InvalidBackendDefinitionException if there is error initializing the backend.
     */
    @Throws(InvalidBackendDefinitionException::class)
    fun init(definition: Definition): Mono<Void> {
        return Mono.empty()
    }

    /**
     * cleanup required by backend can be thrown in here
     */
    fun destroy(): Mono<Void> {
        return Mono.empty()
    }

    /**
     * Searches backend fingerprint store, return prints that match the provided criteria.
     *
     * @param query query containing the search parameters
     * @param types helps us decide what types we which to search against
     */
    fun search(query: Query, types: Array<DataType> = arrayOf(DataType.IMAGE), sdk: IBiometricSDKAdapter? = null): Flux<Identity>

    /**
     * Searches backend fingerprint store, and returns the positions of fingers for a given person stored in backend in
     * order of their quality.
     *
     * @param filters the search matching filters.
     */
    fun positions(filters: Map<String, String>): Flux<FingerPosition>

    /**
     * Helper function that pings backend, testing for connectivity.
     */
    fun healthcheck(query: Query): Mono<Boolean>

    /**
     * Enables backends to validate definitions, ensuring that any options they need to work are provided.
     */
    @Throws(InvalidBackendDefinitionException::class)
    fun validateDefinition(definition: Definition)

    @Throws(InvalidBackendDefinitionException::class)
    fun validateFilter(filter: Map<String, Any>)
}
