package org.kiva.identityservice.services.backends

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.kiva.identityservice.domain.FingerPosition
import org.kiva.identityservice.errorhandling.exceptions.InvalidBackendException
import org.kiva.identityservice.errorhandling.exceptions.api.InvalidQueryFilterException
import org.kiva.identityservice.generateQuery
import org.kiva.identityservice.services.backends.drivers.SqlBackend
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureWebTestClient
class BackendManagerTest {

    @Autowired
    private lateinit var backendManager: IBackendManager

    @Test
    @Throws(Exception::class)
    fun initialize() {
        assertTrue(backendManager.filtersAllFields("template").containsAll(listOf("nationalId", "voterId", "firstName")))

        assertTrue(backendManager.validFingerPositions("template").contains(FingerPosition.RIGHT_THUMB))
        assertTrue(backendManager.validFingerPositions("template").contains(FingerPosition.LEFT_THUMB))
        assertTrue(backendManager.validFingerPositions("template").contains(FingerPosition.RIGHT_INDEX))
        assertTrue(backendManager.validFingerPositions("template").contains(FingerPosition.RIGHT_MIDDLE))
        assertTrue(backendManager.validFingerPositions("template").contains(FingerPosition.RIGHT_RING))
        assertTrue(backendManager.validFingerPositions("template").contains(FingerPosition.RIGHT_PINKY))
        assertTrue(backendManager.validFingerPositions("template").contains(FingerPosition.LEFT_INDEX))
        assertTrue(backendManager.validFingerPositions("template").contains(FingerPosition.LEFT_MIDDLE))
        assertTrue(backendManager.validFingerPositions("template").contains(FingerPosition.LEFT_RING))
        assertTrue(backendManager.validFingerPositions("template").contains(FingerPosition.LEFT_PINKY))

        assertTrue(backendManager.filtersRequiredFields("template").containsAll(listOf()))

        assertTrue(backendManager.filtersUniqueFields("template").containsAll(listOf("nationalId", "voterId", "firstName")))

        assertTrue(backendManager.filtersListFields("template").containsAll(listOf("voterId")))

        assertTrue(backendManager.filtersHashedFields("template").containsAll(listOf("nationalId", "voterId")))

        // let's test filter mapping
        val templateFilterMapping = backendManager.filtersMapping("template")
        assertTrue(templateFilterMapping.containsKey("nationalId"))
        assertTrue(templateFilterMapping.containsKey("voterId"))
        assertTrue(templateFilterMapping.containsKey("firstName"))
        assertEquals(Pair("national_id", Operator.EQUAL), templateFilterMapping["nationalId"])
        assertEquals(Pair("voter_id", Operator.IN), templateFilterMapping["voterId"])
        assertEquals(Pair("first_name", Operator.FUZZY), templateFilterMapping["firstName"])

        // let's test backend
        assertTrue(backendManager.getbyName("template") is SqlBackend)
    }

    @Disabled
    @Test
    @Throws(Exception::class)
    fun validateFilterMissingKeys() {
        val filter = HashMap<String, String>()
        filter["firstName"] = FIRST_NAME

        assertThrows<InvalidQueryFilterException> {
            backendManager.validateQuery(query.copy(backend = "template", filters = filter))
        }
    }

    @Test
    @Throws(Exception::class)
    fun invalidBackendName() {
        val filter = HashMap<String, String>()

        assertThrows<InvalidBackendException> {
            backendManager.validateQuery(query.copy(backend = "dummy", filters = filter))
        }
    }

    @Test
    @Throws(Exception::class)
    fun validateFilterUniqueKeys() {
        val filter = HashMap<String, String>()
        filter["firstName"] = FIRST_NAME
        filter["nationalId"] = NATIONAL_ID
        assertThrows<InvalidQueryFilterException> {
            backendManager.validateQuery(query.copy(backend = "template", filters = filter))
        }
    }

    @Test
    @Throws(Exception::class)
    fun validateFilterNonDeclaredFields() {
        val filter = HashMap<String, String>()
        filter["firstNameddsd"] = FIRST_NAME
        assertThrows<InvalidQueryFilterException> {
            backendManager.validateQuery(query.copy(backend = "template", filters = filter))
        }
    }

    @Test
    @Throws(Exception::class)
    fun validateFilterOk() {
        val filter = HashMap<String, String>()
        filter["nationalId"] = FIRST_NAME
        backendManager.validateQuery(query.copy(backend = "template", filters = filter))
    }

    @Disabled
    @Test
    fun validateQueryFingerpositions() {
        assertThrows<InvalidQueryFilterException> {
            backendManager.validateQuery(query.copy(backend = "template", position = FingerPosition.LEFT_INDEX))
        }
    }

    /** The sample first name used in this test. */
    private val FIRST_NAME = "first_name"

    /** The sample national id used in this test. */
    private val NATIONAL_ID = "112222"

    /** The sample query used in this test. */
    private val query = generateQuery()
}
