package org.kiva.identityservice.services.backends.drivers

import org.kiva.identityservice.domain.DataType
import org.kiva.identityservice.domain.FingerPosition
import org.kiva.identityservice.domain.Identity
import org.kiva.identityservice.domain.Query
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

    var config: MutableMap<String, Any> = mutableMapOf()
    override var requiredFilters: MutableSet<String> = mutableSetOf()
    override var uniqueFilters: MutableSet<String> = mutableSetOf()
    override var listFilters: MutableSet<String> = mutableSetOf()
    override var hashedFilters: MutableSet<String> = mutableSetOf()
    override var filters: MutableSet<String> = mutableSetOf()
    override var validFingerPositions: MutableSet<FingerPosition> = mutableSetOf()
    override var filterMappers: MutableMap<String, Pair<String, Operator>> = mutableMapOf()

    protected val BACKEND_INITILIZAE_ERROR_MESSAGE = "Error initializing the backend."

    /**
     * initialization tasks required by backend.
     */
    override fun init(definition: Definition): Mono<Void> {
        try {
            definition.config["config"]
                .children()
                .forEach {
                    config = it.asMap<String, Any>()
                }
            return super.init(definition)
        } catch (ex: Exception) {
            return Mono.error(InvalidBackendDefinitionException(BACKEND_INITILIZAE_ERROR_MESSAGE))
        }
    }

    /**
     * Should search backend fingerprint store and return prints that match the provided criteria
     *
     * @param query the search query.
     * @param types the data types.
     * @param sdk the backend biometric matching service.
     */
    override fun search(query: Query, types: Array<DataType>, sdk: IBiometricSDKAdapter?): Flux<Identity> {
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
    override fun healthcheck(query: Query): Mono<Boolean> {
        return true.toMono()
    }

    /**
     * Enables backends to validate definitions, ensuring that options they need to work is provided
     */
    @Throws(InvalidBackendDefinitionException::class)
    override fun validateDefinition(definition: Definition) {
        // let's ensure that we have our connection parameters set in our backend
        definition.config["config"]
            .children()
            .forEach {
                val config = it.asMap<String, Any>()
                for (key in listOf("host", "port", "database", "table", "user", "password")) {
                    if (!config.containsKey(key)) {
                        throw InvalidBackendDefinitionException("Missing backend config key $key needed by " + javaClass.name)
                    }
                }
            }
    }

    @Throws(InvalidBackendDefinitionException::class)
    override fun validateFilter(filter: Map<String, Any>) {
    }
}
