package org.kiva.bioauthservice.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.Application
import io.ktor.util.KtorExperimentalAPI
import org.flywaydb.core.Flyway
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.kiva.bioauthservice.db.repositories.ReplayRepository
import org.kiva.bioauthservice.util.getLong
import org.kiva.bioauthservice.util.getString
import org.slf4j.Logger

class DbPort(override val jdbi: Jdbi) : ReplayRepository

// Retrieving a child config is an experimental API, subject to renaming in the future
@KtorExperimentalAPI
fun Application.initDB(logger: Logger): DbPort {

    // Set up Hikari connection pool
    val dbConfig = environment.config.config("db")
    val config = HikariConfig() // TODO: This can be a properties file, or hardcoded here
    config.jdbcUrl = dbConfig.getString("url")
    config.username = dbConfig.getString("username")
    config.password = dbConfig.getString("password")
    config.connectionTimeout = dbConfig.getLong("connectionTimeout")
    config.idleTimeout = dbConfig.getLong("idleTimeout")
    config.maxLifetime = dbConfig.getLong("maxLifetime")
    val ds = HikariDataSource(config)

    // Set up Flyway
    val flyway: Flyway = Flyway.configure().dataSource(ds).load()
    if (flyway.info().pending().isNotEmpty()) {
        logger.info("Starting to run migrations")
        flyway.migrate()
        logger.info("Migrations done")
    } else {
        logger.info("No migrations to run")
    }

    // Set up JDBI
    val jdbi = Jdbi.create(ds)
    jdbi.installPlugin(KotlinPlugin())
    return DbPort(jdbi)
}