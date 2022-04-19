package org.kiva.bioanalyzerservice.api

import datadog.trace.api.Trace
import org.kiva.bioanalyzerservice.domain.AnalysisType
import org.kiva.bioanalyzerservice.domain.Stats
import org.kiva.bioanalyzerservice.services.BioDataAnalysisEngine
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.ZonedDateTime
import javax.validation.Valid

@RequestMapping("/api/v1")
@RestController
class ApiController constructor(private val analysisEngine: BioDataAnalysisEngine) {

    private val startedAt = ZonedDateTime.now()

    /**
     * Test endpoint to ensure bioanalyzer is up and running.
     */
    @GetMapping("/healthz")
    fun healthCheck(): Mono<ResponseEntity<String>> {
        return ResponseEntity.ok("OK").toMono()
    }

    /**
     * Endpoint to get some basic stats about the bioanalyzer
     */
    @GetMapping("/stats")
    fun stats(): Mono<ResponseEntity<Stats>> {
        val stats = Stats(
            serviceName = "bioanalyzer-service",
            startedAt = startedAt,
            currentTime = ZonedDateTime.now(),
            versions = listOf("none")
        )
        return ResponseEntity.ok(stats).toMono()
    }

    /**
     * @param queryMap a map of query keyed by the name of the image or something unique associated with the result
     */
    @Trace
    @PostMapping("/analyze", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun verify(@Valid @RequestBody queryMap: Map<String, Query>): Mono<MutableMap<String, Map<AnalysisType, Any>>> {
        try {
            return Flux.fromIterable(queryMap.entries)
                .onBackpressureBuffer()
                .flatMap {
                    analysisEngine.execute(it.value.imageByte, it.value.type, it.value.analysis)
                        .map { result -> Pair<String, Map<AnalysisType, Any>>(it.key, result) }
                }
                .collectMap(Pair<String, Map<AnalysisType, Any>>::first, Pair<String, Map<AnalysisType, Any>>::second)
        } catch (e: Exception) {
            return Mono.error(e)
        }
    }
}
