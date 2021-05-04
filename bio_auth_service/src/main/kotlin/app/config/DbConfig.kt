package org.kiva.bioauthservice.app.config

import com.zaxxer.hikari.HikariConfig
import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.common.utils.getLong
import org.kiva.bioauthservice.common.utils.getString

@KtorExperimentalAPI
class DbConfig(baseConfig: ApplicationConfig) {
    val hashPepper: String = baseConfig.getString("hashPepper")
    val jdbcUrl: String = baseConfig.getString("url")
    val username: String = baseConfig.getString("username")
    val password: String = baseConfig.getString("password")
    val connectionTimeout: Long = baseConfig.getLong("connectionTimeout")
    val idleTimeout: Long = baseConfig.getLong("idleTimeout")
    val maxLifetime: Long = baseConfig.getLong("maxLifetime")

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
