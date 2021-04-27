package org.kiva.bioauthservice.fingerprint.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.kiva.bioauthservice.errors.impl.InvalidFingerPositionException

@Serializable
enum class FingerPosition constructor(val code: Int) {

    @SerialName("1") RIGHT_THUMB(1),
    @SerialName("2") RIGHT_INDEX(2),
    @SerialName("3") RIGHT_MIDDLE(3),
    @SerialName("4") RIGHT_RING(4),
    @SerialName("5") RIGHT_PINKY(5),
    @SerialName("6") LEFT_THUMB(6),
    @SerialName("7") LEFT_INDEX(7),
    @SerialName("8") LEFT_MIDDLE(8),
    @SerialName("9") LEFT_RING(9),
    @SerialName("10") LEFT_PINKY(10);

    override fun toString(): String {
        return code.toString()
    }

    companion object {
        fun fromCode(value: Int?): FingerPosition {
            val exception = InvalidFingerPositionException(
                "Invalid position, must be one of " + values().joinToString(", ") { i -> i.toString() }
            )
            if (value == null) {
                throw exception
            }
            for (position in values()) {
                if (value == position.code) return position
            }

            throw exception
        }
    }
}
