package org.kiva.bioanalyzerservice.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

/**
 * Type of bio data e.g fingerprint, iris
 */
enum class BioType private constructor(val code: String) {

    FINGERPRINT("fingerprint");

    @JsonValue
    override fun toString(): String {
        return code
    }

    companion object {

        @JsonCreator
        fun fromCode(value: String?): BioType {
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
