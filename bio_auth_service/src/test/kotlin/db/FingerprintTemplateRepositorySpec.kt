package db

import alphanumericStringGen
import com.machinezoo.sourceafis.FingerprintTemplate
import com.opentable.db.postgres.embedded.EmbeddedPostgres
import io.kotest.core.spec.style.StringSpec
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

    "should be able to insert a template" {
        val dto = SaveRequestDto(
            did1,
            SaveRequestParamsDto(1, captureDate, position)
        )
        val result = repo.insertTemplate(dto, template)
        result shouldBe true
    }

    "should be able to insert a second template" {
        val dto = SaveRequestDto(
            did2,
            SaveRequestParamsDto(1, captureDate, position)
        )
        val result = repo.insertTemplate(dto, template)
        result shouldBe true
    }

    "should be able to insert a record with a missing template" {
        val dto = SaveRequestDto(
            did3,
            SaveRequestParamsDto(1, captureDate, position, missing_code = missingCode)
        )
        val result = repo.insertTemplate(dto)
        result shouldBe true
    }

    "should retrieve no templates if the filters match nothing" {
        val result = repo.getTemplates(VerifyRequestFiltersDto("foobar"), position)
        result.size shouldBe 0
    }

    "should be able to retrieve a single inserted template by did" {
        val filters = VerifyRequestFiltersDto(dids = did3)
        val result = repo.getTemplates(filters, position)
        result shouldHaveSize 1
        val dao = result.first()
        dao.id shouldBe 3
        dao.did shouldBe did3
    }

    "should be able to return multiple templates if there are multiple matches by did" {
        val filters = VerifyRequestFiltersDto(dids = "$did1,$did2")
        val result = repo.getTemplates(filters, position)
        result shouldHaveSize 2
    }

    "should be able to get positions by did" {
        val dto = PositionsDto(dids = did2)
        val result = repo.getPositions(dto)
        result shouldHaveSize 1
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
