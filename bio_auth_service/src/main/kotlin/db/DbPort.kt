package org.kiva.bioauthservice.db

import org.jdbi.v3.core.Jdbi
import org.kiva.bioauthservice.db.repositories.ReplayRepository

class DbPort(override val jdbi: Jdbi) : ReplayRepository
