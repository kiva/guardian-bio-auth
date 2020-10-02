package org.kiva.identityservice.errorhandling.exceptions

class InvalidBackendFieldsDefinitionException
/**
 * Constructs a new exception with backend name as its detail message.
 * The cause is not initialized, and may subsequently be initialized by a call to [.initCause].
 */
(private val backend: String, private val fields: List<String>) : InvalidBackendDefinitionException(backend) {

    override fun toString(): String {
        return StringBuilder("Backend [")
                .append(backend)
                .append("] configuration definition is missing fields: ").append(fields.joinToString(", ")).toString()
    }
}
