package org.kiva.identityservice

import org.kiva.identityservice.domain.DataType
import org.kiva.identityservice.domain.FingerPosition
import org.kiva.identityservice.domain.Identity
import org.kiva.identityservice.domain.VerifyRequest
import org.kiva.identityservice.utils.base64ToByte
import org.kiva.identityservice.utils.loadBase64FromResource
import org.kiva.identityservice.utils.loadBytesFromResource
import java.util.Base64
import kotlin.collections.HashMap

/**
 * The utility functions used by different tests.
 */

/**
 * Helper function that returns a query.
 */
fun generateQuery(imageType: DataType = DataType.IMAGE): VerifyRequest {
    val imageBase64 = loadBase64FromResource(IMAGE1)
    val filters = HashMap<String, String>()
    filters["nationalId"] = NATIONAL_ID

    val q = VerifyRequest("template", imageBase64, FINGER_POSITION, filters, imageType)
    q.imageByte = base64ToByte(imageBase64)
    return q
}

/**
 * Helper function that returns a query with template input.
 */
fun generateTemplateQuery(template: String): VerifyRequest {
    val templateBase64 = Base64.getEncoder().encodeToString(template.toByteArray())
    val filters = HashMap<String, String>()
    filters["nationalId"] = NATIONAL_ID

    val q = VerifyRequest("template", templateBase64, FINGER_POSITION, filters, DataType.TEMPLATE)
    q.imageByte = base64ToByte(templateBase64)
    return q
}

/**
 * Helper function that returns a sample Identity.
 *
 * @param did the did of the identity.
 * @param fingerImage the fingerprint image.
 * @param fingerPosition the fingerprint position.
 * @return the sample Identity data.
 */
fun generateIdentity(did: String, fingerImage: ByteArray, fingerPosition: FingerPosition = FINGER_POSITION): Identity {
    return Identity(did, NATIONAL_ID, mapOf(fingerPosition to fingerImage), DataType.IMAGE, 0)
}

/**
 * Helper function that returns a list of sample identities.
 *
 * @param count the number of asked identities.
 */
fun generateIdentities(count: Int): MutableList<Identity> {
    val identities: MutableList<Identity> = mutableListOf()
    for (i in 0 until count) {
        val identity = Identity(i.toString(), "", mapOf(FINGER_POSITION to loadBytesFromResource(IMAGE2)), DataType.IMAGE, 0)
        identities.add(identity)
    }
    return identities
}

/** The sample national id used in this test. */
private val NATIONAL_ID = "112222"

/** The sample fingerprint image1. */
private val IMAGE1 = "images/fingerprint.jpg"

/** The sample fingerprint image2. */
private val IMAGE2 = "images/fingerprint.png"

/** The finger position used in this test. */
private val FINGER_POSITION = FingerPosition.RIGHT_THUMB
