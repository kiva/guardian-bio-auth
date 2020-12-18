package org.kiva.identityservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [R2dbcAutoConfiguration::class])
@EnableConfigurationProperties
class IdentityServiceApplication

fun main(args: Array<String>) {
    try {
        runApplication<IdentityServiceApplication>(*args)
    } finally {
    }
}
