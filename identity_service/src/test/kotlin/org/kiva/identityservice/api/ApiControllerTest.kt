package org.kiva.identityservice.api

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kiva.identityservice.domain.FingerPosition
import org.kiva.identityservice.domain.Fingerprint
import org.kiva.identityservice.domain.Query
import org.kiva.identityservice.utils.loadBytesFromResource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import java.sql.Timestamp

@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureWebTestClient
@EnableAutoConfiguration(exclude = [R2dbcAutoConfiguration::class])
class ApiControllerTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    private val BASE_END_POINT = "/api/v1"

    /**
     * Tests the healthz endpoint.
     *
     * @throws Exception if there is any.
     */
    @Test
    fun verifyHealth() {
        webTestClient.get().uri("$BASE_END_POINT/healthz")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(String::class.java)
            .hasSize(1)
            .contains("OK")
    }

    /**
     * Tests the verify endpoint.
     * STEP1: Generate template for a given national id.
     * STEP2: Verify the fingerprint image for the the voter id and it should return match.
     * STEP3: Verify the fingerprint image for the the list of voter ids and it should return match.
     * STEP4: Verify the fingerprint image for the an invalid voter id and no citizen found is expected.
     *
     * @throws Exception if there is any.
     */
    @Disabled
    @Test
    fun testVerify() {

        val voterId = "VERIFY_VOTER_${System.currentTimeMillis()}"

        // STEP1: Generate template for a given national id.
        val fingerprintStr = String(loadBytesFromResource("images/fingerprint_NIN55555.txt"))
        val fp = Fingerprint(voterId, NATIONAL_ID, DID, 1, FingerPosition.RIGHT_THUMB, null, Timestamp(System.currentTimeMillis()), fingerprintStr)

        webTestClient.post().uri("$BASE_END_POINT/templatizer/bulk/$TEMPLATE_BACKEND")
            .bodyValue(listOf(fp))
            .exchange()
            .expectStatus().isOk

        // STEP2: Verify the fingerprint image for the the national id and it should return match.
        val filters1 = HashMap<String, String>()
        filters1["voterId"] = voterId
        val imageBase64 = String(loadBytesFromResource("images/fingerprint_NIN55555_base64.txt"))
        val query1 = Query(TEMPLATE_BACKEND, imageBase64, FingerPosition.RIGHT_THUMB, filters1)

        webTestClient.post().uri("$BASE_END_POINT/verify")
            .bodyValue(query1)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .json("{\"status\":\"matched\"}")

        // STEP3: Verify the fingerprint image for the the list of national ids and it should return match.
        val filters2 = HashMap<String, String>()
        filters2["voterId"] = "VOTER1,$voterId,VOTER2"
        val query2 = Query(TEMPLATE_BACKEND, imageBase64, FingerPosition.RIGHT_THUMB, filters2)

        webTestClient.post().uri("$BASE_END_POINT/verify")
            .bodyValue(query2)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .json("{\"status\":\"matched\"}")

        // STEP4: Verify the fingerprint image for the an invalid voter id and no citizen found is expected.
        val filters3 = HashMap<String, String>()
        filters3["voterId"] = INVALID_VOTER_ID
        val query3 = Query(TEMPLATE_BACKEND, imageBase64, FingerPosition.RIGHT_THUMB, filters3)

        webTestClient.post().uri("$BASE_END_POINT/verify")
            .bodyValue(query3)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .json("{\"code\":\"NO_CITIZEN_FOUND\"}")
    }

    /**
     * Tests the backend endpoint for template backend.
     *
     * @throws Exception if there is any.
     */
    @Test
    fun verifyBackendForTemplate() {
        webTestClient.get().uri("$BASE_END_POINT/backend/$TEMPLATE_BACKEND")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .json("{\"positions\":[\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"10\"],\"filters\":[\"firstName\",\"nationalId\",\"voterId\"]}")
    }

    /**
     * Tests the backend endpoint for invalid backend name.
     *
     * @throws Exception if there is any.
     */
    @Test
    fun verifyBackendForInvalidName() {
        webTestClient.get().uri("$BASE_END_POINT/backend/$INVALID_BACKEND")
            .exchange()
            .expectStatus().isNotFound
    }

    /**
     * Tests the positions endpoint.
     * STEP1- Call positions for a test voter id and since there is no record in templatedb it returns empty list
     * STEP2- Insert a sample record for right thumb finger in template db and this time the positions endpoint should return it
     * STEP3- Insert another sample record for left thumb finger in template db and this time the positions endpoint should return two items
     * STEP4- Insert another missing finger record for right index in template db and this time the positions endpoint should return two items again
     *
     * @throws Exception if there is any.
     */
    @Disabled
    @Test
    fun testPositions() {

        val voterId = "POSITION_VOTER_${System.currentTimeMillis()}"
        // STEP1- Call positions for a test nationalId and since there is no record in templatedb it returns empty list
        webTestClient.get().uri("$BASE_END_POINT/positions/$TEMPLATE_BACKEND/nationalId=$voterId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .json("[]")

        // STEP2- Insert a sample record for right thumb finger in template db and this time the positions endpoint should return it
        val fingerprintStr = String(loadBytesFromResource("images/fingerprint_NIN55555.txt"))
        val fp1 = Fingerprint(voterId, "ID_POSITION", "DID_POSITION", 1, FingerPosition.RIGHT_THUMB, null, Timestamp(System.currentTimeMillis()), fingerprintStr)

        webTestClient.post().uri("$BASE_END_POINT/templatizer/bulk/$TEMPLATE_BACKEND")
            .bodyValue(listOf(fp1))
            .exchange()
            .expectStatus().isOk

        webTestClient.get().uri("$BASE_END_POINT/positions/$TEMPLATE_BACKEND/voterId=$voterId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .json("[\"1\"]")

        // STEP3- Insert another sample record for left thumb finger in template db and this time the positions endpoint should return two items
        val fp2 = Fingerprint(voterId, "ID_POSITION", "DID_POSITION", 1, FingerPosition.LEFT_THUMB, null, Timestamp(System.currentTimeMillis()), fingerprintStr)

        webTestClient.post().uri("$BASE_END_POINT/templatizer/bulk/$TEMPLATE_BACKEND")
            .bodyValue(listOf(fp2))
            .exchange()
            .expectStatus().isOk

        webTestClient.get().uri("$BASE_END_POINT/positions/$TEMPLATE_BACKEND/voterId=$voterId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .json("[\"1\",\"6\"]")

        // STEP4- Insert another missing finger record for right index in template db and this time the positions endpoint should return two items again
        val fp3 = Fingerprint(voterId, "ID_POSITION", "DID_POSITION", 1, FingerPosition.RIGHT_INDEX, "NA", Timestamp(System.currentTimeMillis()), null)

        webTestClient.post().uri("$BASE_END_POINT/templatizer/bulk/$TEMPLATE_BACKEND")
            .bodyValue(listOf(fp3))
            .exchange()
            .expectStatus().isOk

        webTestClient.get().uri("$BASE_END_POINT/positions/$TEMPLATE_BACKEND/voterId=$voterId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .json("[\"1\",\"6\"]")
    }

    /** The template backend name. */
    private val TEMPLATE_BACKEND = "template"

    /** The invalid backend name. */
    private val INVALID_BACKEND = "invalid"

    /** The sample national id used in this test. */
    private val NATIONAL_ID = "NIN55555"

    /** The sample invalid national id used in this test. */
    private val INVALID_VOTER_ID = "invalid"

    /** The sample did used in this test. */
    private val DID = "14hnHFRjaiwVjZVtZPsPCv"
}
