package org.kiva.identityservice.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class ResponseStatus constructor(val code: String) {

    MATCHED("matched"), NOT_MATCHED("not_matched");

    @JsonValue
    override fun toString(): String {
        return code
    }

    companion object {

        @JsonCreator
        @JvmStatic
        fun fromCode(value: String?): ResponseStatus {
            if (value == null) {
                throw IllegalArgumentException()
            }
            for (position in values()) {
                if (value.equals(position.code, ignoreCase = true)) return position
            }

            throw IllegalArgumentException()
        }
    }
}
