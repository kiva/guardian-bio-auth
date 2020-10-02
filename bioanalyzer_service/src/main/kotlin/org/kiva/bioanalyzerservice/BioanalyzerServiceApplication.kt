package org.kiva.bioanalyzerservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BioanalyzerServiceApplication

fun main(args: Array<String>) {
    runApplication<BioanalyzerServiceApplication>(*args)
}
