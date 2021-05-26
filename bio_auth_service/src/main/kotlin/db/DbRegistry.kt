package org.kiva.bioauthservice.db

import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.Application
import io.ktor.util.KtorExperimentalAPI
import org.flywaydb.core.Flyway
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.kiva.bioauthservice.app.AppRegistry
import org.kiva.bioauthservice.db.repositories.FingerprintTemplateRepository
import org.kiva.bioauthservice.db.repositories.ReplayRepository

@KtorExperimentalAPI
fun Application.registerDB(appRegistry: AppRegistry): DbRegistry {
    val logger = appRegistry.logger
    val dbConfig = appRegistry.appConfig.dbConfig

    // Set up Hikari connection pool
    val ds = HikariDataSource(dbConfig.hikariConfig())

    // Set up Flyway (includes configuring it to set a baseline in case it migrates against an existing database - by default, V1)
    val flyway = Flyway
        .configure()
        .baselineOnMigrate(true)
        .dataSource(ds)
        .load()

    // If running on an empty db
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
    val fingerprintTemplateRepository = FingerprintTemplateRepository(jdbi, logger)

    return DbRegistry(replayRepository, fingerprintTemplateRepository)
}

@KtorExperimentalAPI
data class DbRegistry(
    val replayRepository: ReplayRepository,
    val fingerprintTemplateRepository: FingerprintTemplateRepository
)
