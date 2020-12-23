package org.kiva.identityservice.services.backends

import org.kiva.identityservice.domain.FingerPosition
import org.kiva.identityservice.domain.VerifyRequest
import org.kiva.identityservice.errorhandling.exceptions.InvalidBackendException
import org.kiva.identityservice.errorhandling.exceptions.api.InvalidFilterException
import org.springframework.beans.factory.InitializingBean

/**
 * Manages backends that the service can fetch identity records from
 */
interface IBackendManager : InitializingBean {

    /**
     * Logic initializing and loading backends should be thrown in here so we don't have a situation wherein our
     * constructor throws an exception
     */
    @Throws(Exception::class)
    fun initialize()

    @Throws(InvalidBackendException::class)
    fun getbyName(name: String): IBackend

    @Throws(InvalidBackendException::class)
    fun filtersAllFields(backend: String): Set<String>

    @Throws(InvalidBackendException::class)
    fun filtersUniqueFields(backend: String): Set<String>

    @Throws(InvalidBackendException::class)
    fun filtersHashedFields(backend: String): Set<String>

    @Throws(InvalidBackendException::class)
    fun filtersListFields(backend: String): Set<String>

    @Throws(InvalidBackendException::class)
    fun filtersRequiredFields(backend: String): Set<String>

    @Throws(InvalidBackendException::class)
    fun filtersMapping(backend: String): Map<String, Pair<String, Operator>>

    @Throws(InvalidBackendException::class)
    fun validFingerPositions(backend: String): Set<FingerPosition>

    @Throws(InvalidFilterException::class, InvalidBackendException::class)
    fun validateVerifyRequest(verifyRequest: VerifyRequest)

    fun all(): List<IBackend>
}
