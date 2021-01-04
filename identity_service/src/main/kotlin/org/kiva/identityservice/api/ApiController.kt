package org.kiva.identityservice.api

import datadog.trace.api.Trace
import org.jnbis.api.Jnbis
import org.kiva.identityservice.domain.BulkSaveRequest
import org.kiva.identityservice.domain.DataType
import org.kiva.identityservice.domain.FingerPosition
import org.kiva.identityservice.domain.Fingerprint
import org.kiva.identityservice.domain.SaveRequest
import org.kiva.identityservice.domain.VerifyRequest
import org.kiva.identityservice.errorhandling.exceptions.FingerprintTemplateGenerationException
import org.kiva.identityservice.errorhandling.exceptions.InvalidBackendException
import org.kiva.identityservice.errorhandling.exceptions.InvalidBackendOperationException
import org.kiva.identityservice.errorhandling.exceptions.api.InvalidFilterException
import org.kiva.identityservice.services.ITemplatizer
import org.kiva.identityservice.services.IVerificationEngine
import org.kiva.identityservice.services.backends.IBackend
import org.kiva.identityservice.services.backends.IBackendManager
import org.kiva.identityservice.services.backends.IHasTemplateSupport
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import javax.validation.Valid

@RequestMapping("/api/v1")
@RestController
class ApiController constructor(
    private val verificationEngine: IVerificationEngine,
    private val backendManager: IBackendManager,
    private val templatizer: ITemplatizer
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Test endpoint to check the status of identity_service.
     */
    @GetMapping("/healthz")
    fun healthCheck(): ResponseEntity<String> {
        return ResponseEntity.ok("OK")
    }

    @Trace
    @PostMapping("/verify", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun verify(@Valid @RequestBody verifyRequest: VerifyRequest): Mono<ResponseEntity<Response>> {
        try {
            return verificationEngine.match(verifyRequest)
                .map { res -> Response(ResponseStatus.MATCHED, res.did, res.did, res.national_id, res.matchingScore) }
                .doOnNext { logger.info("Sending Response: $it") }
                .map { res -> ResponseEntity.ok(res) }
        } catch (e: Exception) {
            return Mono.error(e)
        }
    }

    /**
     * @return the positions of fingers for a given person stored in backend in the order of their quality.
     * Sample GET call: /positions/template/nationalId=ABCD
     */
    @GetMapping("/positions/{backend}/{filter}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun positions(
        @PathVariable("backend") backend: String,
        @PathVariable("filter") filter: String
    ): Flux<FingerPosition> {
        try {
            val filters = filter.split("=")
            if (filters.size != 2)
                return Flux.error(InvalidFilterException("One of your filters is invalid or missing. Filter has to be in the format 'national_id=123'"))

            val backendObj: IBackend = backendManager.getbyName(backend)

            if (filters[0] !in backendObj.uniqueFilters)
                return Flux.error(InvalidFilterException("Filter '{$filters[0]}' has to be a unique type e.g. national_id"))

            val filtersMap: Map<String, String> = mapOf(filters[0] to filters[1])
            return backendObj.positions(filtersMap)
        } catch (e: Exception) {
            return Flux.error(e)
        }
    }

    /**
     * The endpoint that accepts array of data (typically ten data rows for each of the citizens fingerprints).
     * It creates a SourceAFIS template for each row and save it to the backend.
     *
     * @param records the list of data for citizens containing identification as well as fingerprint images.
     */
    @PostMapping(
        "/templatizer/bulk/{backend}",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.ALL_VALUE]
    )
    fun templatizerGenerateForFingerprints(
        @PathVariable("backend") backendName: String,
        @Valid @RequestBody records: List<Fingerprint>
    ): Mono<ResponseEntity<out Any>> {
        try {
            val backend = backendManager.getbyName(backendName)
            if (backend !is IHasTemplateSupport)
                return Mono.just(
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body("'$backendName' does not support templates")
                )

            // will not throw exception if quality is too low.  this allows images to be saved regardless of quality
            return templatizer.bulkGenerate(false, backend, records).map { ResponseEntity.ok(it) }
        } catch (e: InvalidBackendException) {
            return Mono.just(ResponseEntity.notFound().build())
        } catch (e: InvalidBackendOperationException) {
            return Mono.error(e)
        } catch (e: FingerprintTemplateGenerationException) {
            return Mono.error(e)
        }
    }

    /**
     * The endpoint that accepts a wrapped array of fingerprint data (either templates or images, can be mixed) and
     * saves each one to the backend. If an image is provided, it will compute the relevant template and quality score
     * so that no images are ever saved. Typically, 10 entries will be provided for each of the citizen's fingerprints,
     * but the endpoint can handle as many as 1000 entries.
     *
     * At some future point, we may remove support for templating images so that our servers never even handle images.
     *
     * @param bulkSaveRequest A wrapper around the array of fingerprints to save.
     */
    @PostMapping("/save", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun save(@Valid @RequestBody bulkSaveRequest: BulkSaveRequest): Mono<ResponseEntity<out Any>> {
        try {
            // Get a backend to save fingerprints in
            val backend = backendManager.getbyName("template")
            if (backend !is IHasTemplateSupport)
                return Mono.just(
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body("'template' does not support templates")
                )

            // Save templates
            val templatesToSave: List<SaveRequest> = bulkSaveRequest.fingerprints.filter { it.params.type == DataType.TEMPLATE }
            val bulkStoreResult = templatizer.bulkSave(backend, templatesToSave)

            // Save images
            val records: List<Fingerprint> = bulkSaveRequest.fingerprints
                .filter { it.params.type == DataType.IMAGE }
                .map {
                    Fingerprint(
                        it.filters.voter_id,
                        it.id,
                        it.filters.national_id,
                        it.params.type_id,
                        it.params.position,
                        it.params.missing_code,
                        it.params.capture_date,
                        it.params.image
                    )
                }
            // will not throw exception if quality is too low.  this allows images to be saved regardless of quality
            val bulkGenerateResult = templatizer.bulkGenerate(false, backend, records)

            // Combine and return results
            return bulkGenerateResult
                .mergeWith(bulkStoreResult)
                .reduce { left, right -> left + right }
                .map { ResponseEntity.ok(it) }
        } catch (e: InvalidBackendOperationException) {
            return Mono.error(e)
        } catch (e: FingerprintTemplateGenerationException) {
            return Mono.error(e)
        }
    }

    /**
     * Returns a print for a citizen for an index
     */
    @GetMapping("/fingerprint_image/{backend}/{filter}/{position}", produces = [MediaType.IMAGE_JPEG_VALUE])
    fun fingerprintImage(
        @PathVariable("backend") backend: String,
        @PathVariable("filter") filter: String,
        @PathVariable("position") position: Int
    ): Mono<ResponseEntity<ByteArray>> {
        try {
            val filters = filter.split("=")
            if (filters.size != 2)
                return Mono.error(InvalidFilterException("One of your filters is invalid or missing. Filter has to be in the format 'national_id=123'"))

            val backendObj: IBackend = backendManager.getbyName(backend)

            if (filters[0] !in backendObj.uniqueFilters)
                return Mono.error(InvalidFilterException("Filter '{$filters[0]}' has to be a unique type e.g. national_id"))

            val fingerPosition: FingerPosition = FingerPosition.fromCode(position)

            return backendObj.search(
                VerifyRequest(backend, "", fingerPosition, mapOf(filters[0] to filters[1])),
                arrayOf(DataType.IMAGE)
            )
                .map { Jnbis.wsq().decode(it.fingerprints[fingerPosition]).toJpg().asByteArray() }
                .singleOrEmpty()
                .map { ResponseEntity.ok(it!!) }
                .defaultIfEmpty(ResponseEntity.noContent().build())
        } catch (e: InvalidBackendException) {
            return Mono.just(ResponseEntity.notFound().build())
        } catch (e: InvalidFilterException) {
            return Mono.error(InvalidFilterException("Fingerprint position has to be between 1 and 10"))
        }
    }

    /**
     * Returns some configuration values of the backend to clients. Example use case is a client
     * wanting to know the list of supported finger positions
     */
    @GetMapping("/backend/{name}")
    fun backend(@PathVariable("name") name: String): Mono<ResponseEntity<MutableMap<String, Any>>> {
        try {
            backendManager.getbyName(name)

            val response: MutableMap<String, Any> = mutableMapOf(
                "positions" to backendManager.validFingerPositions(name),
                "filters" to backendManager.filtersAllFields(name)
            )
            return ResponseEntity.ok(response).toMono()
        } catch (e: InvalidBackendException) {
            return Mono.just(ResponseEntity.notFound().build())
        }
    }
}
