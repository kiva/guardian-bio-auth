package db

import alphanumericStringGen
import com.machinezoo.sourceafis.FingerprintTemplate
import com.opentable.db.postgres.embedded.EmbeddedPostgres
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.arbitrary.next
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.flywaydb.core.Flyway
import org.jdbi.v3.core.Jdbi
import org.kiva.bioauthservice.common.utils.base64ToByte
import org.kiva.bioauthservice.db.repositories.FingerprintTemplateRepository
import org.kiva.bioauthservice.fingerprint.dtos.PositionsDto
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestParamsDto
import org.kiva.bioauthservice.fingerprint.dtos.VerifyRequestFiltersDto
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition
import org.slf4j.LoggerFactory
import java.time.ZonedDateTime

@ExperimentalSerializationApi
@KtorExperimentalAPI
class FingerprintTemplateRepositorySpec : StringSpec({

    // Set up DB
    val pg: EmbeddedPostgres = EmbeddedPostgres.start()
    val flyway = Flyway.configure().dataSource(pg.postgresDatabase).load()
    flyway.migrate()
    val jdbi = Jdbi.create(pg.postgresDatabase)
    jdbi.installPlugins()

    // Test fixtures
    val template = FingerprintTemplate(this.javaClass.getResource("/images/sample_source_afis_template.txt")?.readText()!!.base64ToByte())
    val logger = LoggerFactory.getLogger(this.javaClass)
    val repo = FingerprintTemplateRepository(jdbi, logger)
    val did1 = alphanumericStringGen.next()
    val did2 = alphanumericStringGen.next()
    val did3 = alphanumericStringGen.next()
    val position = FingerPosition.RIGHT_INDEX
    val captureDate = ZonedDateTime.now()
    val missingCode = "XX"

    /*
     * Insert template tests
     */

    "should be able to insert a template" {
        val dto = SaveRequestDto(
            did1,
            SaveRequestParamsDto(1, captureDate, position, quality_score = 90.0)
        )
        val result = repo.insertTemplate(dto, template)
        result shouldBe true
    }

    "should be able to insert a second template" {
        val dto = SaveRequestDto(
            did2,
            SaveRequestParamsDto(1, captureDate, position, quality_score = 80.0)
        )
        val result = repo.insertTemplate(dto, template)
        result shouldBe true
    }

    "should be able to insert a second template for a did that already has an inserted template" {
        val dto = SaveRequestDto(
            did2,
            SaveRequestParamsDto(1, captureDate, FingerPosition.RIGHT_MIDDLE, quality_score = 70.0)
        )
        val result = repo.insertTemplate(dto, template)
        result shouldBe true
    }

    "should be able to insert a record with a missing template" {
        val dto = SaveRequestDto(
            did3,
            SaveRequestParamsDto(1, captureDate, position, quality_score = 70.0, missing_code = missingCode)
        )
        val result = repo.insertTemplate(dto)
        result shouldBe true
    }

    /*
     * Get templates tests
     */

    "should retrieve no templates if the filters match nothing" {
        val result = repo.getTemplates(VerifyRequestFiltersDto("foobar"), position)
        result.size shouldBe 0
    }

    "should be able to retrieve a single inserted template for a did" {
        val filters = VerifyRequestFiltersDto(dids = did1)
        val result = repo.getTemplates(filters, position)
        result shouldHaveSize 1
        val dao = result.first()
        dao.id shouldBe 1
        dao.did shouldBe did1
    }

    "should be able to return multiple templates if there are matches for multiple dids" {
        val filters = VerifyRequestFiltersDto(dids = "$did1,$did3")
        val result = repo.getTemplates(filters, position)
        result shouldHaveSize 2
    }

    /*
     * Get positions tests
     */

    "should be able to get a single position matching a single did" {
        val dto = PositionsDto(dids = did1)
        val result = repo.getPositions(dto)
        result shouldHaveSize 1
        result shouldContain FingerPosition.RIGHT_INDEX
    }

    "should be able to get multiple positions matching a single did, sorted by quality_score" {
        val dto = PositionsDto(dids = did2)
        val result = repo.getPositions(dto)
        result shouldHaveSize 2
        result shouldContainInOrder listOf(FingerPosition.RIGHT_INDEX, FingerPosition.RIGHT_MIDDLE)
    }

    "should be able to get multiple positions matching multiple dids" {
        val dto = PositionsDto(dids = "$did1,$did2")
        val result = repo.getPositions(dto)
        result shouldHaveSize 3
    }

    "should retrieve no positions if the only valid results have a missing_code" {
        val dto = PositionsDto(dids = did3)
        val result = repo.getPositions(dto)
        result shouldHaveSize 0
    }

    "should retrieve no positions if the filters match nothing" {
        val result = repo.getPositions(PositionsDto("foobar"))
        result shouldHaveSize 0
    }
})
