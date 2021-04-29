package org.kiva.bioauthservice.fingerprint.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ResponseStatus constructor(val code: String) {

    @SerialName("matched") MATCHED("matched"),
    @SerialName("not_matched") NOT_MATCHED("not_matched");

    override fun toString(): String {
        return code
    }

    companion object {
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
