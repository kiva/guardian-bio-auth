package org.kiva.bioauthservice.app.config

import com.zaxxer.hikari.HikariConfig
import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.common.utils.getLong
import org.kiva.bioauthservice.common.utils.getString

@KtorExperimentalAPI
class DbConfig(baseConfig: ApplicationConfig) {
    val hashPepper = baseConfig.getString("hashPepper")
    val jdbcUrl = baseConfig.getString("url")
    val username = baseConfig.getString("username")
    val password = baseConfig.getString("password")
    val connectionTimeout = baseConfig.getLong("connectionTimeout")
    val idleTimeout = baseConfig.getLong("idleTimeout")
    val maxLifetime = baseConfig.getLong("maxLifetime")

    fun hikariConfig(): HikariConfig {
        val config = HikariConfig()
        config.jdbcUrl = jdbcUrl
        config.username = username
        config.password = password
        config.connectionTimeout = connectionTimeout
        config.idleTimeout = idleTimeout
        config.maxLifetime = maxLifetime
        return config
    }
}
