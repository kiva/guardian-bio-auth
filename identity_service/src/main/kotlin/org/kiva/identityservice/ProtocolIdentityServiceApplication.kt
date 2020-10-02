package org.kiva.identityservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties
class IdentityServiceApplication

fun main(args: Array<String>) {
    try {
        runApplication<IdentityServiceApplication>(*args)
    } finally {
    }
}
