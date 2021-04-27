package org.kiva.bioauthservice.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.Application
import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import org.flywaydb.core.Flyway
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.kiva.bioauthservice.util.getLong
import org.kiva.bioauthservice.util.getString
import org.slf4j.Logger

// ApplicationConfig utils use an experimental API
@KtorExperimentalAPI
private fun ApplicationConfig.toHikariConfig(): HikariConfig {
    val config = HikariConfig()
    config.jdbcUrl = this.getString("url")
    config.username = this.getString("username")
    config.password = this.getString("password")
    config.connectionTimeout = this.getLong("connectionTimeout")
    config.idleTimeout = this.getLong("idleTimeout")
    config.maxLifetime = this.getLong("maxLifetime")
    return config
}

// Uses toHikariAPI, which itself uses an experimental API. Plus getting a child config is an experimental API, subject to future renaming.
@KtorExperimentalAPI
fun Application.registerDB(logger: Logger): DbRegistry {

    // Set up Hikari connection pool
    val baseConfig = environment.config.config("db")
    val ds = HikariDataSource(baseConfig.toHikariConfig())

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
    val dbPort = DbPort(jdbi)

    return DbRegistry(dbPort)
}

data class DbRegistry(val dbPort: DbPort)
