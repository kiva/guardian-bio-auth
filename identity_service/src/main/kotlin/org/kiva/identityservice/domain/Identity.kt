package org.kiva.identityservice.domain

data class Identity
    (val did: String?, val national_id: String?, val fingerprints: Map<FingerPosition, ByteArray>, val type: DataType, val templateVersion: Int?) {

    override fun equals(other: Any?): Boolean {
        return if (other is Identity) this.national_id.equals(other.national_id) else false
    }

    override fun toString(): String {
        return "Identity(id='$national_id', fingerprints=${fingerprints.keys})"
    }

    var matchingScore: Double = 0.0
        set(value: Double) {
            if (value >= 0) {
                field = value
            }
        }
}
