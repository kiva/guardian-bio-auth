package org.kiva.bioauthservice.db

import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.Application
import io.ktor.util.KtorExperimentalAPI
import org.flywaydb.core.Flyway
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.kiva.bioauthservice.db.repositories.FingerprintTemplateRepository
import org.kiva.bioauthservice.db.repositories.ReplayRepository
import org.slf4j.Logger

@KtorExperimentalAPI
fun Application.registerDB(logger: Logger): DbRegistry {

    // Set up Config
    val baseConfig = environment.config.config("db")
    val dbConfig = DbConfig(baseConfig)

    // Set up Hikari connection pool
    val ds = HikariDataSource(dbConfig.hikariConfig())

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

    // Set up Repositories
    val replayRepository = ReplayRepository(jdbi, logger)
    val fingerprintTemplateRepository = FingerprintTemplateRepository(jdbi, dbConfig, logger)

    return DbRegistry(replayRepository, fingerprintTemplateRepository)
}

@KtorExperimentalAPI
data class DbRegistry(
    val replayRepository: ReplayRepository,
    val fingerprintTemplateRepository: FingerprintTemplateRepository
)
