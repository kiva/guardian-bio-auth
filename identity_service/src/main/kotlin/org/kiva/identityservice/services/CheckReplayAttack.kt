package org.kiva.identityservice.services

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.kiva.identityservice.domain.Query
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.MessageDigest

@Service
class CheckReplayAttack : ICheckReplayAttack {

    private val logger = LoggerFactory.getLogger(javaClass)

    object ImageTable : Table("fingerprint_images") {
        val id = integer("image_id").primaryKey().autoIncrement()
        val hashCode = varchar("hash_code", 255).uniqueIndex("unique_codes")
        val time = datetime("time_seen")
        val count = integer("count_seen")
    }

    override fun isReplayAttack(query: Query) {
        val runBReplayAttacheCheck = try {
            System.getenv("REPLAY_ATTACK_ENABLED").toBoolean()
        } catch (ex: Exception) {
            false
        }

        if (runBReplayAttacheCheck) {
            val hashValue = computeHash(query)
            checkDB(hashValue)
        }
    }

    private fun computeHash(query: Query): String {
        val bytes = query.image.toByteArray()
        val md = MessageDigest.getInstance("SHA-512")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

    private fun checkDB(hashValue: String) {
        // if not in the database, add it.  store: hash value, timestamp, # of times seen
        // if in the database, increment count.  probably a replay attack -- do something  (for now, just log it).

        Database.connect(
            System.getenv("IDENTITYINTELLIGENCEDB_POSTGRES_URL"),
            driver = System.getenv("IDENTITYINTELLIGENCEDB_POSTGRES_DRIVER"),
            user = System.getenv("IDENTITYINTELLIGENCEDB_POSTGRES_USER"),
            password = System.getenv("IDENTITYINTELLIGENCEDB_POSTGRES_PASSWORD")
        )

        transaction {
            SchemaUtils.create(ImageTable)
            val conn = TransactionManager.current().connection
            val statement = conn.createStatement()

            val query = "INSERT INTO fingerprint_images (hash_code) VALUES ('$hashValue') \n" +
                "ON CONFLICT (hash_code) DO UPDATE \n" +
                "SET count_seen=fingerprint_images.count_seen + 1, \n" +
                "\t\ttime_seen=CURRENT_TIMESTAMP"

            statement.execute(query)

            val row = ImageTable
                .select({ ImageTable.hashCode eq hashValue })
                .first()

            val countSeen = row[ImageTable.count]
            val timeSeen = row[ImageTable.time]

            if (countSeen > 1) {
                logger.info("Possible replay attack seen at $timeSeen. Image has been used $countSeen times.")
            }
        }
    }
}
