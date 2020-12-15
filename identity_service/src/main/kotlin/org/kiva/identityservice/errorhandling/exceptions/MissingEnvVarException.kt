package org.kiva.identityservice.errorhandling.exceptions

class MissingEnvVarException(
    private val envVarName: String
) : Exception() {
    override val message: String
        get() = "Expected Environment Variable $envVarName to be defined."
}