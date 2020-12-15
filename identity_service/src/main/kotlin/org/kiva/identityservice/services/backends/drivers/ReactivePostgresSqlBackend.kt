package org.kiva.identityservice.services.backends.drivers

import io.r2dbc.client.R2dbc
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.spi.Row
import java.util.Random
import kotlin.streams.asSequence
import org.jooq.Condition
import org.jooq.Record
import org.jooq.SQLDialect
import org.jooq.SelectQuery
import org.jooq.conf.ParamType
import org.jooq.impl.DSL
import org.jooq.impl.DSL.condition
import org.jooq.impl.DSL.field
import org.kiva.identityservice.config.EnvConfig
import org.kiva.identityservice.domain.DataType
import org.kiva.identityservice.domain.Identity
import org.kiva.identityservice.domain.Query
import org.kiva.identityservice.errorhandling.exceptions.InvalidBackendDefinitionException
import org.kiva.identityservice.services.backends.Definition
import org.kiva.identityservice.services.backends.Operator
import org.kiva.identityservice.services.sdks.IBiometricSDKAdapter
import org.kiva.identityservice.utils.generateHash
import org.kiva.identityservice.utils.generateHashForList
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Driver for fetching identity records from an sql backend
 */
abstract class ReactivePostgresSqlBackend(
    private val env: EnvConfig,
    private val port: Int,
    private val host: String,
    private val table: String,
    private val database: String?,
    private val user: String?,
    private val password: String?
) : SqlBackend() {
    lateinit var client: R2dbc
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${namkha.backend.fetch_limit}")
    private var fetchLimit: Long? = null

    /**
     * Initialization tasks required by backend.
     */
    override fun init(definition: Definition): Mono<Void> {

        try {
            val res = super.init(definition)

            val builder = PostgresqlConnectionConfiguration
                .builder()
                .host(host)
                .port(port)
                .database(database)
            user?.let { builder.username(it) }
            password?.let { builder.password(it) }

            client = R2dbc(
                PostgresqlConnectionFactory(builder.build())
            )
            return res
        } catch (ex: Exception) {
            return Mono.error(InvalidBackendDefinitionException(backendInitializeErrorMsg))
        }
    }

    /**
     * Searches backend fingerprint store, return prints that match a criteria for comparison.
     * @todo only supports equality matches for now.
     *
     * @param query the search query.
     * @param types the data types.
     * @param sdk the backend biometric matching service.
     * @return
     */
    override fun search(query: Query, types: Array<DataType>, sdk: IBiometricSDKAdapter?): Flux<Identity> {
        var builder = DSL.using(SQLDialect.POSTGRES)
            .select()
            .from((table).trim()).query

        for (entry in query.filters) {
            // @TODO we want to coerce into preferred datatype here
            // let's get operator and mapped field
            val column = table + "." + filterMappers[entry.key]!!.first
            val operator = filterMappers[entry.key]!!.second
            var value = entry.value.trim()

            // let's hash the filter value if it is a hashed filter.
            if (hashedFilters.contains(entry.key)) {
                /*
                 * If the value is a comma separated items, we should hash each single item of them.
                 * For list of v1,v2,v3,v4, the following code will return HASH(v1),HASH(v2),HASH(v3)
                 */
                value = if (listFilters.contains(entry.key)) {
                    generateHashForList(entry.value.split(','), env.hashPepper).joinToString(",")
                } else {
                    generateHash(entry.value, env.hashPepper)
                }
            }

            if (operator == Operator.FUZZY) {
                val alias = randomAlias()
                // let's set up sorting
                builder.addSelect(field("word_similarity('$value', $column) as $alias"))
                builder.addOrderBy(field(alias).desc())

                // let's set up filter
                builder.addConditions(condition("$column <% ?", value))
            } else {
                if (listFilters.contains(entry.key)) {
                    /*
                     * For comma separated list of items, we generate a query chain of OR conditions for matching that list.
                     * For instance if we are looking for voter id in list of 'voter1, voter2, voter3' teh following code will generate the following condition
                     * (voterId = voter1) OR (voterId = voter2) OR (voterId = voter3)
                     */
                    val values = value.split(',')
                    var conditions: Condition = DSL.falseCondition()
                    for (item in values) {
                        conditions = conditions.or(condition("$column = ?", item))
                    }
                    builder.addConditions(conditions)
                } else {
                    builder.addConditions(condition("$column ${operator.code} ?", value))
                }
            }
        }
        // let's give implementations a chance to edit this
        builder = customize(builder, query, types, sdk)
        fetchLimit?.let { builder.addLimit(it.toInt()) }

        logger.info("Running query: " + builder.getSQL(ParamType.INLINED))

        return client
            .withHandle { handle ->
                handle.select(builder.getSQL(ParamType.INLINED))
                    .mapRow { row -> handleResult(row, query) }
                    .doOnNext { logger.info("Fetched Identity $it") }
            }
    }

    private fun randomAlias(): String {
        val source = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return Random().ints(3, 0, source.length)
            .asSequence()
            .map(source::get)
            .joinToString("")
    }

    abstract fun handleResult(row: Row, query: Query): Identity

    /**
     * drivers wishing to provide more specific implementation can override this and add to query.
     */
    open fun customize(sqlQuery: SelectQuery<Record>, query: Query, type: Array<DataType>, sdk: IBiometricSDKAdapter?) =
        sqlQuery

    /**
     * Helper function that pings backend, testing for connectivity.
     */
    override fun healthcheck(query: Query): Mono<Boolean> {
        return client
            .withHandle {
                it.select("SELECT version();")
                    .mapRow { row -> row["version"] }
            }.hasElements()
    }
}
