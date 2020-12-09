package org.kiva.identityservice.errorhandling.exceptions

/**
 * Constructs a new exception with backend name as its detail message.
 * The cause is not initialized, and may subsequently be initialized by a call to [.initCause].
 */
class InvalidBackendFieldsDefinitionException(
    private val backend: String,
    private val fields: List<String>
) : InvalidBackendDefinitionException(backend) {
    override fun toString(): String = "Backend [$backend] configuration definition is missing fields: ${fields.joinToString(", ")}"
}
