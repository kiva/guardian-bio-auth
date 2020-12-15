package org.kiva.identityservice.services.backends

import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.kiva.identityservice.domain.FingerPosition
import org.kiva.identityservice.errorhandling.exceptions.InvalidBackendException
import org.kiva.identityservice.errorhandling.exceptions.api.InvalidQueryFilterException
import org.kiva.identityservice.generateQuery
import org.kiva.identityservice.services.backends.drivers.SqlBackend
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest
@RunWith(SpringRunner::class)
@AutoConfigureWebTestClient
class BackendManagerTest {

    @Autowired
    private lateinit var backendManager: IBackendManager

    @Test
    @Throws(Exception::class)
    fun initialize() {
        Assert.assertTrue(backendManager.filtersAllFields("template").containsAll(listOf("nationalId", "voterId", "firstName")))

        Assert.assertTrue(backendManager.validFingerPositions("template").contains(FingerPosition.RIGHT_THUMB))
        Assert.assertTrue(backendManager.validFingerPositions("template").contains(FingerPosition.LEFT_THUMB))
        Assert.assertTrue(backendManager.validFingerPositions("template").contains(FingerPosition.RIGHT_INDEX))
        Assert.assertTrue(backendManager.validFingerPositions("template").contains(FingerPosition.RIGHT_MIDDLE))
        Assert.assertTrue(backendManager.validFingerPositions("template").contains(FingerPosition.RIGHT_RING))
        Assert.assertTrue(backendManager.validFingerPositions("template").contains(FingerPosition.RIGHT_PINKY))
        Assert.assertTrue(backendManager.validFingerPositions("template").contains(FingerPosition.LEFT_INDEX))
        Assert.assertTrue(backendManager.validFingerPositions("template").contains(FingerPosition.LEFT_MIDDLE))
        Assert.assertTrue(backendManager.validFingerPositions("template").contains(FingerPosition.LEFT_RING))
        Assert.assertTrue(backendManager.validFingerPositions("template").contains(FingerPosition.LEFT_PINKY))

        Assert.assertTrue(backendManager.filtersRequiredFields("template").containsAll(listOf()))

        Assert.assertTrue(backendManager.filtersUniqueFields("template").containsAll(listOf("nationalId", "voterId", "firstName")))

        Assert.assertTrue(backendManager.filtersListFields("template").containsAll(listOf("voterId")))

        Assert.assertTrue(backendManager.filtersHashedFields("template").containsAll(listOf("nationalId", "voterId")))

        // let's test filter mapping
        val templateFilterMapping = backendManager.filtersMapping("template")
        Assert.assertTrue(templateFilterMapping.containsKey("nationalId"))
        Assert.assertTrue(templateFilterMapping.containsKey("voterId"))
        Assert.assertTrue(templateFilterMapping.containsKey("firstName"))
        Assert.assertThat(templateFilterMapping["nationalId"], CoreMatchers.`is`(Pair("national_id", Operator.EQUAL)))
        Assert.assertThat(templateFilterMapping["voterId"], CoreMatchers.`is`(Pair("voter_id", Operator.IN)))
        Assert.assertThat(templateFilterMapping["firstName"], CoreMatchers.`is`(Pair("first_name", Operator.FUZZY)))

        // let's test backend
        Assert.assertThat(backendManager.getbyName("template"), instanceOf(SqlBackend::class.java))
    }

    @Ignore
    @Test(expected = InvalidQueryFilterException::class)
    @Throws(Exception::class)
    fun validateFilterMissingKeys() {
        val filter = HashMap<String, String>()
        filter["firstName"] = FIRST_NAME

        backendManager.validateQuery(query.copy(backend = "template", filters = filter))
    }

    @Test(expected = InvalidBackendException::class)
    @Throws(Exception::class)
    fun invalidBackendName() {
        val filter = HashMap<String, String>()

        backendManager.validateQuery(query.copy(backend = "dummy", filters = filter))
    }

    @Test(expected = InvalidQueryFilterException::class)
    @Throws(Exception::class)
    fun validateFilterUniqueKeys() {
        val filter = HashMap<String, String>()
        filter["firstName"] = FIRST_NAME
        filter["nationalId"] = NATIONAL_ID
        backendManager.validateQuery(query.copy(backend = "template", filters = filter))
    }

    @Test(expected = InvalidQueryFilterException::class)
    @Throws(Exception::class)
    fun validateFilterNonDeclaredFields() {
        val filter = HashMap<String, String>()
        filter["firstNameddsd"] = FIRST_NAME
        backendManager.validateQuery(query.copy(backend = "template", filters = filter))
    }

    @Test
    @Throws(Exception::class)
    fun validateFilterOk() {
        val filter = HashMap<String, String>()
        filter["nationalId"] = FIRST_NAME
        backendManager.validateQuery(query.copy(backend = "template", filters = filter))
    }

    @Ignore
    @Test(expected = InvalidQueryFilterException::class)
    fun validateQueryFingerpositions() {
        backendManager.validateQuery(query.copy(backend = "template", position = FingerPosition.LEFT_INDEX))
    }

    @Test(expected = InvalidQueryFilterException::class)
    @Throws(Exception::class)
    fun validateDids() {
        val dids = mutableListOf<String>()
        for (i in 0 until 101) {
            dids.add(DID)
        }
        backendManager.validateQuery(query.copy(dids = dids.toList()))
    }

    /** The sample first name used in this test. */
    private val FIRST_NAME = "first_name"

    /** The sample national id used in this test. */
    private val NATIONAL_ID = "112222"

    /** The sample DID used in this test. */
    private val DID = "14hnHFRjaiwVjZVtZPsPCv"

    /** The sample query used in this test. */
    private val query = generateQuery()
}
