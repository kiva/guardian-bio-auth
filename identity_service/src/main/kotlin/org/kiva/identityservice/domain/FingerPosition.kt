package org.kiva.identityservice.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import org.kiva.identityservice.errorhandling.exceptions.api.InvalidFingerPositionException

enum class FingerPosition constructor(val code: Int) {

    RIGHT_THUMB(1),
    RIGHT_INDEX(2),
    RIGHT_MIDDLE(3),
    RIGHT_RING(4),
    RIGHT_PINKY(5),
    LEFT_THUMB(6),
    LEFT_INDEX(7),
    LEFT_MIDDLE(8),
    LEFT_RING(9),
    LEFT_PINKY(10);

    @JsonValue
    override fun toString(): String {
        return code.toString()
    }

    companion object {

        @JvmStatic
        @JsonCreator
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
