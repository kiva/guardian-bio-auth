package org.kiva.identityservice.services.sdks.sourceafis

import org.assertj.core.util.Arrays
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test
import org.kiva.identityservice.generateIdentities
import org.kiva.identityservice.generateIdentity
import org.kiva.identityservice.generateQuery
import org.kiva.identityservice.utils.base64ToByte
import org.kiva.identityservice.utils.loadBase64FromResource
import org.kiva.identityservice.utils.loadBytesFromResource
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

class SourceAFISFingerprintSDKAdapterTest {

    /**
     * Tests the case when there is a match among list of identities.
     */
    @Test
    @Throws(Exception::class)
    fun testMatch() {
        val identities = generateIdentities(20)
        identities.add(generateIdentity(DID1, query.imageByte))
        StepVerifier.create(sdk.match(query, Flux.fromIterable(identities)))
                .expectSubscription()
                .assertNext {
                    Assert.assertThat(it.did, CoreMatchers.`is`(DID1))
                    Assert.assertTrue(it.matchingScore >= MATCHING_THRESHOLD)
                }
                .verifyComplete()
    }

    /**
     * Tests the case when there is a match and there us only one identity.
     */
    @Test
    @Throws(Exception::class)
    fun testOnlyMatch() {
        val identity = generateIdentity(DID1, query.imageByte)
        StepVerifier.create(sdk.match(query, Flux.fromArray(Arrays.array(identity))))
                .expectSubscription()
                .assertNext {
                    Assert.assertThat(it.did, CoreMatchers.`is`(DID1))
                    Assert.assertTrue(it.matchingScore >= MATCHING_THRESHOLD)
                }
                .verifyComplete()
    }

    /**
     * Tests the case when there are several matches. The order of returned identities is also sorted from low quality
     * match to high quality match. In this test, the sdk will not return the very low quality fingerprint since it is
     * lower than matching threshold.
     */
    @Test
    @Throws(Exception::class)
    fun testMultipleMatch() {
        val identities = generateIdentities(20)
        val identity1 = generateIdentity(DID1, query.imageByte)
        val identity2 = generateIdentity(DID2, base64ToByte(loadBase64FromResource(VERY_LOW_QUALITY_IMAGE)))
        val identity3 = generateIdentity(DID3, base64ToByte(loadBase64FromResource(LOW_QUALITY_IMAGE)))
        identities.add(identity1)
        identities.add(identity2)
        identities.add(identity3)
        StepVerifier.create(sdk.match(query, Flux.fromIterable(identities)))
                .expectSubscription()
                .assertNext {
                    Assert.assertThat("Invalid identity record returned", it.did, CoreMatchers.`is`(DID3))
                    Assert.assertEquals("Invalid identity record returned", it.matchingScore, identity3.matchingScore, 0.0)
                }
                .assertNext {
                    Assert.assertThat("Invalid identity record returned", it.did, CoreMatchers.`is`(DID1))
                    Assert.assertEquals("Invalid identity record returned", it.matchingScore, identity1.matchingScore, 0.0)
                }
                .verifyComplete()

        /**
         * The matching sdk should run for all identity records and the matching score should be set for all identities.
         */
        Assert.assertTrue("Invalid matching score returned", identity1.matchingScore >= MATCHING_THRESHOLD)
        Assert.assertTrue("Invalid matching score returned", identity2.matchingScore < MATCHING_THRESHOLD)
        Assert.assertTrue("Invalid matching score returned", identity3.matchingScore >= MATCHING_THRESHOLD)
        Assert.assertTrue("Higher quality fingerprint should have higher matching score as well", identity1.matchingScore > identity3.matchingScore)
    }

    /**
     * Tests the case when there is no match.
     */
    @Test
    @Throws(Exception::class)
    fun testNoMatch() {
        val identities = generateIdentities(20)
        identities.add(generateIdentity(DID1, loadBytesFromResource(IMAGE_FILE)))
        StepVerifier.create(sdk.match(query, Flux.fromIterable(identities)))
                .expectNextCount(0)
                .verifyComplete()
    }

    /** The fingerprint sdk adapter matching threshold. */
    private val MATCHING_THRESHOLD = 40.0

    /** The fingerprint sdk adapter used in this test. */
    private val sdk = SourceAFISFingerprintSDKAdapter(MATCHING_THRESHOLD)

    /** The sample low quality fingerprint image. */
    private val LOW_QUALITY_IMAGE = "images/fingerprint_low_quality.jpg"

    /** The sample very low quality fingerprint image. */
    private val VERY_LOW_QUALITY_IMAGE = "images/fingerprint_very_low_quality.jpg"

    /** The sample did used in this test. */
    private val DID1 = "123"

    /** The sample did used in this test. */
    private val DID2 = "457"

    /** The sample did used in this test. */
    private val DID3 = "789"

    /** The sample fingerprint image2. */
    private val IMAGE_FILE = "images/fingerprint.png"

    /** The sample query used in this test. */
    private val query = generateQuery()
}
