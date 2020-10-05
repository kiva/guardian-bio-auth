package org.kiva.identityservice.domain

import java.sql.Timestamp

/**
 * The data class for handling fingerprint related APIs such as bulk template generation.
 */

data class Fingerprint(
    val voter_id: String?,
    val did: String,
    val national_id: String?,
    val type_id: Int,
    val position: FingerPosition,
    val missing_code: String?,
    val capture_date: Timestamp,
    val image: String?
) {
    override fun toString(): String {
        return "Fingerprint(voter_id='$voter_id', did='$did', national_id='$national_id', type_id=$type_id, position=$position, " +
                "missing_code=$missing_code, capture_date=$capture_date, image=$image)"
    }

    override fun hashCode(): Int {
        var result = voter_id?.hashCode() ?: 0
        result = 31 * result + did.hashCode()
        result = 31 * result + (national_id?.hashCode() ?: 0)
        result = 31 * result + type_id
        result = 31 * result + position.hashCode()
        result = 31 * result + (missing_code?.hashCode() ?: 0)
        result = 31 * result + capture_date.hashCode()
        result = 31 * result + (image?.hashCode() ?: 0)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Fingerprint

        if (did != other.did) return false
        if (position != other.position) return false

        return true
    }
}
