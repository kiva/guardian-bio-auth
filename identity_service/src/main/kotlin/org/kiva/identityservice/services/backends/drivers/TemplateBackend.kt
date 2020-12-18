package org.kiva.identityservice.services.backends.drivers

import com.machinezoo.sourceafis.FingerprintTemplate
import io.r2dbc.spi.Row
import org.jooq.Record
import org.jooq.SQLDialect
import org.jooq.SelectQuery
import org.jooq.conf.ParamType
import org.jooq.impl.DSL
import org.jooq.impl.DSL.condition
import org.jooq.impl.DSL.field
import org.kiva.identityservice.config.EnvConfig
import org.kiva.identityservice.domain.DataType
import org.kiva.identityservice.domain.FingerPosition
import org.kiva.identityservice.domain.Fingerprint
import org.kiva.identityservice.domain.Identity
import org.kiva.identityservice.domain.Query
import org.kiva.identityservice.domain.StoreRequest
import org.kiva.identityservice.errorhandling.exceptions.FingerprintTemplateGenerationException
import org.kiva.identityservice.errorhandling.exceptions.ImageDecodeException
import org.kiva.identityservice.errorhandling.exceptions.InvalidBackendOperationException
import org.kiva.identityservice.errorhandling.exceptions.api.ApiExceptionCode
import org.kiva.identityservice.errorhandling.exceptions.api.FingerprintMissingAmputationException
import org.kiva.identityservice.errorhandling.exceptions.api.FingerprintMissingNotCapturedException
import org.kiva.identityservice.errorhandling.exceptions.api.FingerprintMissingUnableToPrintException
import org.kiva.identityservice.errorhandling.exceptions.api.ValidationError
import org.kiva.identityservice.services.IBioAnalyzer
import org.kiva.identityservice.services.backends.IHasTemplateSupport
import org.kiva.identityservice.services.backends.Operator
import org.kiva.identityservice.services.sdks.IBiometricSDKAdapter
import org.kiva.identityservice.utils.byteToBase64
import org.kiva.identityservice.utils.decodeImage
import org.kiva.identityservice.utils.generateHash
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Driver for fetching identity records and storing template from template backend.
 */
@Component
class TemplateBackend(private val env: EnvConfig) :
    ReactivePostgresSqlBackend(
        env,
        env.identityDbTemplatePostgresPort,
        env.identityDbTemplatePostgresHost,
        env.identityDbTemplateCitizenTable,
        env.identityDbTemplatePostgresDb,
        env.identityDbTemplatePostgresUser,
        env.identityDbTemplatePostgresPassword
    ),
    IHasTemplateSupport {

    private val logger = LoggerFactory.getLogger(javaClass)

    /** The fuzzy search for positions error message. */
    private val POSITION_FUZZY_SEARCH_ERROR = "Fuzzy search is not supported calling positions operation."

    /** The list field search for positions error message. */
    private val POSITION_LIST_SEARCH_ERROR = "List field search is not supported calling positions operation."

    /**
     * allows to add the joins needed.
     */
    override fun customize(sqlQuery: SelectQuery<Record>, query: Query, types: Array<DataType>, sdk: IBiometricSDKAdapter?): SelectQuery<Record> {
        val q = super.customize(sqlQuery, query, types, sdk)

        q.addSelect(
            field("voter_id"),
            field("national_id"),
            field("did"),
            field("position"),
            field("missing_code")
        )
        q.addConditions(condition("position = ?", query.position.code))
        q.addConditions(condition("type_id = ?", 1))

        if (query.dids.isNotEmpty()) {
            q.addConditions(condition("did in ?", query.dids))
        }

        if (DataType.TEMPLATE in types) {
            q.addSelect(
                field("version"),
                field("template_type"),
                field("template as image"),
                field("'TEMPLATE' as image_type")
            )
            q.addConditions(condition("version = ?", sdk!!.version))
            q.addConditions(condition("template_type = ?", sdk.templateType))
            q.addConditions(condition("type_id = ?", 1))
        } else {
            throw ValidationError(ApiExceptionCode.INVALID_DATA_TYPE, "Image data type is not supported for template backend.")
        }
        return q
    }

    override fun handleResult(row: Row, query: Query): Identity {
        val did = row["did", String::class.java]
        val nationalId = row["national_id", String::class.java]

        val rawImage = row["image", String::class.java]

        if (rawImage == null) {
            val missingCode = row["missing_code", String::class.java]
            when (missingCode) {
                "NA" -> throw FingerprintMissingNotCapturedException()
                "XX" -> throw FingerprintMissingAmputationException()
                "UP" -> throw FingerprintMissingUnableToPrintException()
                else -> throw FingerprintMissingNotCapturedException() // let's default to NA if not available.
            }
        }

        val templateVersion = row["version"]?.let { it as Int } ?: 0

        val type = DataType.valueOf(row["image_type", String::class.java]!!)
        val imageBytes = when (type) {
            DataType.TEMPLATE -> rawImage.toByteArray(Charsets.UTF_8)
            else -> throw ValidationError(ApiExceptionCode.INVALID_DATA_TYPE, "Image data type is not supported for template backend.")
        }
        return Identity(did, nationalId!!, mapOf(query.position to imageBytes), type, templateVersion)
    }

    override fun templateGenerate(records: Flux<Fingerprint>, throwException: Boolean, sdk: IBiometricSDKAdapter, bioAnalyzer: IBioAnalyzer): Flux<Int> {
        return records
            .flatMap { fingerprint ->
                fingerprint.image
                    ?.let { image -> findFingerprintScore(image, throwException, bioAnalyzer) }
                    ?.flatMap { score -> this.templateGenerateHelper(fingerprint, score, sdk) }
                    ?: this.templateGenerateHelper(fingerprint, 0.0, sdk)
            }
    }

    override fun storeTemplate(sdk: IBiometricSDKAdapter, storeRequest: StoreRequest): Mono<FingerprintTemplate> {
        return sdk.buildTemplate(storeRequest.imageByte).flatMap { template ->
            val sqlUpdate = "INSERT INTO kiva_biometric_template(did,position,template_type,type_id,national_id,voter_id,version,capture_date,template,quality_score) " +
                "VALUES($1,$2,$3,$4,$5,$6,$7,$8,$9,$10) " +
                "ON CONFLICT ON CONSTRAINT unique_did_postion_template_constraint DO " +
                "UPDATE SET national_id=$5,voter_id=$6,version=$7,capture_date=$8,missing_code=NULL,template=$9,quality_score=NULL"

            client.inTransaction {
                it.execute(
                    sqlUpdate,
                    storeRequest.did,
                    storeRequest.position.code,
                    sdk.templateType,
                    storeRequest.type_id,
                    storeRequest.national_id,
                    storeRequest.voter_id,
                    sdk.version,
                    storeRequest.capture_date,
                    template,
                    storeRequest.quality_score
                )
                    .doOnNext { count -> logger.debug("$count rows affected by $sqlUpdate") }
                    .last()
                    .map { template }
            }.last()
        }
    }

    /**
     * Finds the fingerprint image quality.
     * @param img the fingerprint image.
     * @param bioAnalyzer the bio analyzer service instance.
     */
    private fun findFingerprintScore(img: String?, throwException: Boolean, bioAnalyzer: IBioAnalyzer): Mono<Double> {
        if (img != null) {
            try {
                /**
                 * Since image in templatizer call is octet encoded, it needs to be converted to base64 format before being
                 * analyzed by bio analyzer service.
                 */
                val imageBytes = decodeImage(img)
                val imageStr = byteToBase64(imageBytes)
                return bioAnalyzer.analyze(imageStr, throwException)
            } catch (ex: Exception) {
                logger.debug("Error in scoring the image : " + ex.message)
            }
        }

        return Mono.just(0.0)
    }

    /**
     * Helper function that generates template for a given fingerprint and updates the corresponding backend.
     *
     * @param fp the fingerprint data record.
     * @param score the fingerprint image quality.
     * @param sdk the backend biometric matching service defines the template version and type.
     *
     * @throws FingerprintTemplateGenerationException if error happens in template generation or template backend update.
     */
    private fun templateGenerateHelper(fp: Fingerprint, score: Any?, sdk: IBiometricSDKAdapter): Mono<Int> {

        try {
            val imgTemplate = fp.image?.let { sdk.buildTemplateFromImage(decodeImage(fp.image)).block() }

            // The nationalId as well as voterId should be hashed before storing in backend.
            val nationalId = generateHash(fp.national_id!!, env.hashPepper)
            val voterId = generateHash(fp.voter_id!!, env.hashPepper)

            if (imgTemplate == null && fp.missing_code != null) {
                val sqlUpdate = "INSERT INTO kiva_biometric_template(did,position,template_type,type_id,national_id,voter_id,version,capture_date,missing_code,quality_score) " +
                    "VALUES($1,$2,$3,$4,$5,$6,$7,$8,$9,$10) " +
                    "ON CONFLICT ON CONSTRAINT unique_did_postion_template_constraint DO " +
                    "UPDATE SET national_id=$5,voter_id=$6,version=$7,capture_date=$8,missing_code=$9,template=NULL,quality_score=$10"

                return client.inTransaction {
                    it.execute(sqlUpdate, fp.did, fp.position.code, sdk.templateType, fp.type_id, nationalId, voterId, sdk.version, fp.capture_date, fp.missing_code, score)
                        .doOnNext { count -> logger.debug("$count rows affected by $sqlUpdate") }.last()
                }.last()
            } else if (imgTemplate != null && fp.missing_code == null) {
                val sqlUpdate = "INSERT INTO kiva_biometric_template(did,position,template_type,type_id,national_id,voter_id,version,capture_date,template,quality_score) " +
                    "VALUES($1,$2,$3,$4,$5,$6,$7,$8,$9,$10) " +
                    "ON CONFLICT ON CONSTRAINT unique_did_postion_template_constraint DO " +
                    "UPDATE SET national_id=$5,voter_id=$6,version=$7,capture_date=$8,missing_code=NULL,template=$9,quality_score=$10"

                return client.inTransaction {
                    it.execute(sqlUpdate, fp.did, fp.position.code, sdk.templateType, fp.type_id, nationalId, voterId, sdk.version, fp.capture_date, imgTemplate, score)
                        .doOnNext { count -> logger.debug("$count rows affected by $sqlUpdate") }.last()
                }.last()
            } else {
                return Mono.error(
                    FingerprintTemplateGenerationException(
                        fp.did, fp.position,
                        "Either fingerprint image or missing code should be present."
                    )
                )
            }
        } catch (ex: ImageDecodeException) {
            return Mono.error(FingerprintTemplateGenerationException(fp.did, fp.position, ex.message!!))
        } catch (ex: Exception) {
            return Mono.error(FingerprintTemplateGenerationException(fp.did, fp.position, ex.message!!))
        }
    }

    /**
     * Searches backend fingerprint store, and returns the positions of fingers for a given person stored in backend in the order of their quality
     * @param filters the search matching filters.
     */
    override fun positions(filters: Map<String, String>): Flux<FingerPosition> {
        val table = env.identityDbTemplateCitizenTable
        val builder = DSL.using(SQLDialect.POSTGRES)
            .select()
            .from((table).trim()).query

        builder.addSelect(
            field("position")
        )

        /**
         * This function only returns finger positions that have fingerprint image.
         */
        builder.addConditions(condition("$table.missing_code IS NULL"))

        /**
         * The finger positions are stored in the order of their quality score.
         */
        builder.addOrderBy(field("quality_score").desc().nullsLast())

        for (entry in filters) {
            // let's get operator and mapped field
            val column = table + "." + filterMappers[entry.key]!!.first
            val operator = filterMappers[entry.key]!!.second
            var value = entry.value.trim()

            // This operation is not supported for list search items
            if (value.split(',').size > 1) {
                return Flux.error(InvalidBackendOperationException(POSITION_LIST_SEARCH_ERROR, "position"))
            }

            // let's hash the filter value if it is a hashed filter.
            if (hashedFilters.contains(entry.key)) {
                value = generateHash(entry.value, env.hashPepper)
            }

            if (operator == Operator.FUZZY) {
                return Flux.error(InvalidBackendOperationException(POSITION_FUZZY_SEARCH_ERROR, "position"))
            } else {
                builder.addConditions(condition("$column = ?", value))
            }
        }

        logger.info("Running query: " + builder.getSQL(ParamType.INLINED))

        return client.withHandle { handle ->
            handle.select(builder.getSQL(ParamType.INLINED))
                .mapRow { row -> FingerPosition.fromCode(row["position"] as Int) }
        }
    }
}
