package org.kiva.identityservice.services.backends

import org.kiva.identityservice.config.EnvConfig
import org.kiva.identityservice.domain.FingerPosition
import org.kiva.identityservice.domain.Query
import org.kiva.identityservice.errorhandling.exceptions.InvalidBackendDefinitionException
import org.kiva.identityservice.errorhandling.exceptions.InvalidBackendException
import org.kiva.identityservice.errorhandling.exceptions.InvalidBackendFieldsDefinitionException
import org.kiva.identityservice.errorhandling.exceptions.InvalidQueryParamsException
import org.kiva.identityservice.errorhandling.exceptions.api.InvalidQueryFilterException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.util.ClassUtils
import java.util.stream.Collectors

@Service
class BackendManager(
    private val loader: IBackendLoader,
    private val env: EnvConfig
) : IBackendManager {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * store definitions of the backends
     */
    private val definitions: MutableMap<String, Definition> = mutableMapOf()

    /**
     * map of load backend drivers
     */
    private val backends: MutableMap<String, IBackend> = mutableMapOf()

    @Throws(Exception::class)
    override fun afterPropertiesSet() {
        initialize()
    }

    /**
     * Logic initializing and loading definitions should be thrown in here so we don't have a situation where-in our constructor throws an exception.
     */
    @Throws(Exception::class)
    override fun initialize() {
        try {
            for (definition in loader.load()) {
                val name = definition.backend

                // let's ensure that we are not redeclaring.
                if (backends.containsKey(name)) {
                    val msg = "Backend '$name' is already declared"
                    logger.error(msg)
                    throw InvalidBackendDefinitionException(msg)
                }

                validateDefinition(name, definition)
                definitions[name] = definition

                // let's load driver
                val backendClass = ClassUtils
                    .forName(definition.config.get("driver").asString(), ClassUtils.getDefaultClassLoader())
                    .asSubclass(IBackend::class.java)

                val ctor = backendClass.constructors[0]
                val backend = ctor.newInstance(env) as IBackend

                // let's give backend chances to validate definition
                backend.validateDefinition(definition)

                // let's init backend
                backend.init(definition)
                backends[name] = backend

                // let's init valid finger positions
                for (i in definition.config.get("positions").`as`(List::class.java)) {
                    try {
                        backend.validFingerPositions.add(FingerPosition.fromCode(i as Int))
                    } catch (e: java.lang.Exception) {
                        val msg = "Defined finger position names should adhere to allowed types"
                        logger.error(msg, e)
                        throw InvalidBackendDefinitionException(msg)
                    }
                }

                // let's build our filter config
                definition.config["filters"]
                    .children()
                    .forEach {
                        val key: String = it.key().asString()
                        backend.filters.add(key)

                        // we can only make the code work by getting as java.lan.Boolean and then casting to kotlin's
                        // if field is required
                        if (it["required"].isPresent && it["required"].`as`(java.lang.Boolean::class.java).booleanValue()) {
                            backend.requiredFilters.add(key)
                        }

                        // if field is unique
                        if (it.get("unique").isPresent && it.get("unique").`as`(java.lang.Boolean::class.java).booleanValue()) {
                            backend.uniqueFilters.add(key)
                        }

                        // if field presents comma separated list
                        if (it["list"].isPresent && it["list"].`as`(java.lang.Boolean::class.java).booleanValue()) {
                            backend.listFilters.add(key)
                        }

                        // if field is hashed
                        if (it["hashed"].isPresent && it["hashed"].`as`(java.lang.Boolean::class.java).booleanValue()) {
                            backend.hashedFilters.add(key)
                        }

                        // let's map each filter to a backend field + operator
                        val toField = if (it.get("to").isPresent)
                            it.get("to").`as`(java.lang.String::class.java).toString() else key
                        val operatorStr = if (it.get("operator").isPresent)
                            it.get("operator").`as`(java.lang.String::class.java).toString() else "="

                        val operator = try {
                            Operator.fromCode(operatorStr)
                        } catch (e: IllegalArgumentException) {
                            val msg = "Operator declared for backend $name filter $key is invalid"
                            logger.error(msg, e)
                            throw InvalidBackendDefinitionException(msg)
                        }
                        backend.filterMappers[key] = Pair(toField, operator)
                    }
            }
            logger.info("Loaded  $backends.size backends: ${backends.keys}")
        } catch (ex: Exception) {
            throw InvalidBackendDefinitionException("Error initializing the backend: ${ex.message}")
        }
    }

    @Throws(InvalidBackendFieldsDefinitionException::class)
    private fun validateDefinition(backend: String, definition: Definition) {
        // let's ensure that we have all the root keys we require
        val missingKeys = ArrayList<String>()
        val availableKeys = definition.config
            .children()
            .map { i -> i.key().asString() }
            .collect(Collectors.toList<String>())

        for (i in 0 until requiredKeys.size - 1) {
            if (!availableKeys.contains(requiredKeys[i])) {
                missingKeys.add(requiredKeys[i])
            }
        }

        if (missingKeys.isNotEmpty()) {
            throw InvalidBackendFieldsDefinitionException(backend, missingKeys)
        }
    }

    @Throws(InvalidBackendException::class)
    override fun getbyName(name: String): IBackend {
        return backends[name] ?: throw InvalidBackendException()
    }

    @Throws(InvalidQueryFilterException::class, InvalidBackendException::class)
    override fun validateQuery(query: Query) {
        val backendName: String = query.backend
        val filters: Map<String, String> = query.filters

        // let's ensure backend exists
        val backend: IBackend = getbyName(backendName)

        // let's ensure that if image is provided at the top-level, it's non-empty
        if (query.image != null && query.image.length == 0) {
            val msg = "Image must be a non-empty string"
            logger.warn(msg)
            throw InvalidQueryParamsException(msg)
        }

        // let's ensure that required filters are provided
        val missingFilters = backend.requiredFilters.filter { !filters.containsKey(it) }
        if (missingFilters.isNotEmpty()) {
            val msg = "Query is missing the required filters: $missingFilters"
            logger.warn(msg)
            throw InvalidQueryFilterException(msg)
        }

        // let's ensure that if unique field is provided, we do not provide any others
        backend.uniqueFilters.forEach { field ->
            if (filters.containsKey(field) && filters.size > 1) {
                val msg = "$field is defined as a unique and can not be specified in query with other params"
                throw InvalidQueryFilterException(msg)
            }
        }

        // let's ensure we don't have params we don't want here
        val validFilters = backend.filters
        val invalidFilters = filters.keys.minus(validFilters)
        if (invalidFilters.isNotEmpty()) {
            val msg = "$invalidFilters are invalid filters; the list of valid filters are $validFilters"
            throw InvalidQueryFilterException(msg)
        }

        // let's ensure we have print position we need
        if (!backend.validFingerPositions.contains(query.params.position)) {
            val msg = "Invalid finger position detected; the list of valid position are ${backend.validFingerPositions}"
            throw InvalidQueryFilterException(msg)
        }

        // let's ensure we aren't provided too many DIDs to attempt to match
        val dids = filters["dids"]?.split(",") ?: emptyList()
        if (dids.size > env.maxDids) {
            val msg = "Too many DIDs to match against; the maximum number of DIDs is ${env.maxDids}"
            throw InvalidQueryFilterException(msg)
        }
    }

    @Throws(InvalidBackendException::class)
    override fun filtersAllFields(backend: String): Set<String> {
        return HashSet(getbyName(backend).filters)
    }

    @Throws(InvalidBackendException::class)
    override fun filtersUniqueFields(backend: String): Set<String> {
        return HashSet(getbyName(backend).uniqueFilters)
    }

    @Throws(InvalidBackendException::class)
    override fun filtersListFields(backend: String): Set<String> {
        return HashSet(getbyName(backend).listFilters)
    }

    @Throws(InvalidBackendException::class)
    override fun filtersHashedFields(backend: String): Set<String> {
        return HashSet(getbyName(backend).hashedFilters)
    }

    @Throws(InvalidBackendException::class)
    override fun validFingerPositions(backend: String): Set<FingerPosition> {
        getbyName(backend)
        return HashSet(getbyName(backend).validFingerPositions)
    }

    @Throws(InvalidBackendException::class)
    override fun filtersRequiredFields(backend: String): Set<String> {
        return HashSet(getbyName(backend).requiredFilters)
    }

    @Throws(InvalidBackendException::class)
    override fun filtersMapping(backend: String): Map<String, Pair<String, Operator>> {
        return getbyName(backend).filterMappers
    }

    companion object {
        private val requiredKeys = arrayOf("name", "driver", "filters", "positions")
    }

    override fun all(): List<IBackend> = backends.values.toList()
}
