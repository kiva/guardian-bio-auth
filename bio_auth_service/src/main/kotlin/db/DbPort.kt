package org.kiva.bioauthservice.db

import io.ktor.util.KtorExperimentalAPI
import org.jdbi.v3.core.Jdbi
import org.kiva.bioauthservice.db.repositories.ReplayRepository

@KtorExperimentalAPI
class DbPort(override val jdbi: Jdbi, dbConfig: DbConfig) : ReplayRepository
