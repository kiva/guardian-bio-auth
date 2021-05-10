package db

import alphanumericStringGen
import com.opentable.db.postgres.embedded.EmbeddedPostgres
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.arbitrary.next
import org.flywaydb.core.Flyway
import org.jdbi.v3.core.Jdbi
import org.kiva.bioauthservice.common.utils.toSha512
import org.kiva.bioauthservice.db.repositories.ReplayRepository
import org.slf4j.LoggerFactory

class ReplayRepositorySpec : StringSpec({

    // Set up DB
    val pg: EmbeddedPostgres = EmbeddedPostgres.start()
    val flyway = Flyway.configure().dataSource(pg.postgresDatabase).load()
    flyway.migrate()
    val jdbi = Jdbi.create(pg.postgresDatabase)
    jdbi.installPlugins()

    // Test fixtures
    val bytes = alphanumericStringGen.next().toByteArray()
    val hashedBytes = bytes.toSha512()
    val logger = LoggerFactory.getLogger(this.javaClass)
    val repo = ReplayRepository(jdbi, logger)

    "adding a new replay should result in a countSeen of 1" {
        val result = repo.addReplay(bytes)
        result.id shouldBe 1
        result.hashCode shouldBe hashedBytes
        result.countSeen shouldBe 1
    }

    "re-adding a replay should result in a countSeen of 2" {
        val result = repo.addReplay(bytes)
        result.id shouldBe 1
        result.hashCode shouldBe hashedBytes
        result.countSeen shouldBe 2
    }
})
