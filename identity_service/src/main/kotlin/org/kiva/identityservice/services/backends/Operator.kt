package org.kiva.identityservice.services.backends

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

/**
 * List of operators that can be used in the backends.yml. Note: we provide a very small immediate subset of what we
 * will need, expanding as needed.
 */
enum class Operator private constructor(val code: String) {

    /**
     * Test for exact match.
     */
    EQUAL("="),

    /**
     * Test for in list match.
     */
    IN("in"),

    /**
     * Test for similarity.
     */
    FUZZY("fuzzy");

    @JsonValue
    override fun toString(): String {
        return code
    }

    companion object {

        @JsonCreator
        fun fromCode(value: String?): Operator {
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
