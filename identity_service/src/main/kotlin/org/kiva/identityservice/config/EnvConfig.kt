package org.kiva.identityservice.config

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import org.kiva.identityservice.errorhandling.exceptions.MissingEnvVarException
import org.springframework.stereotype.Component

/**
 * These are all the environment variables that we expect to be defined.
 */
@Component
class EnvConfig {

    private final val dotenv: Dotenv = dotenv {
        ignoreIfMissing = true // do not require the presence of ".env" (just pull from system environment)
    }

    final val bioanalyzerEnabled: Boolean
    final val bioanalyzerServiceUrl: String
    final val bioanalyzerQualityThreshold: Double

    final val replayAttackEnabled: Boolean
    final val identityIntelligenceDbPostgresUrl: String
    final val identityIntelligenceDbPostgresDriver: String
    final val identityIntelligenceDbPostgresUser: String
    final val identityIntelligenceDbPostgresPassword: String

    final val identityDbTemplatePostgresHost: String
    final val identityDbTemplatePostgresPort: Int
    final val identityDbTemplatePostgresDb: String?
    final val identityDbTemplatePostgresUser: String?
    final val identityDbTemplatePostgresPassword: String?
    final val identityDbTemplateCitizenTable: String

    final val maxDids: Int

    final val hashPepper: String

    constructor() {
        bioanalyzerEnabled = "BIOANALYZER_ENABLED"
            .optionalEnvVar()
            .withDefault({ it.toBoolean() }, false)
        bioanalyzerServiceUrl = "BIOANALYZER_SERVICE_URL"
            .optionalEnvVar()
            .withDefault({ it }, "")
        bioanalyzerQualityThreshold = "BIOANALYZER_QUALITY_THRESHOLD"
            .optionalEnvVar()
            .withDefault({ it.toDouble() }, 0.0)

        replayAttackEnabled = "REPLAY_ATTACK_ENABLED"
            .optionalEnvVar()
            .withDefault({ it.toBoolean() }, false)
        identityIntelligenceDbPostgresUrl = "IDENTITYINTELLIGENCEDB_POSTGRES_URL"
            .optionalEnvVar()
            .withDefault({ it }, "")
        identityIntelligenceDbPostgresDriver = "IDENTITYINTELLIGENCEDB_POSTGRES_DRIVER"
            .optionalEnvVar()
            .withDefault({ it }, "")
        identityIntelligenceDbPostgresUser = "IDENTITYINTELLIGENCEDB_POSTGRES_USER"
            .optionalEnvVar()
            .withDefault({ it }, "")
        identityIntelligenceDbPostgresPassword = "IDENTITYINTELLIGENCEDB_POSTGRES_PASSWORD"
            .optionalEnvVar()
            .withDefault({ it }, "")

        identityDbTemplatePostgresHost = "IDENTITYDB_TEMPLATE_POSTGRES_HOST"
            .optionalEnvVar()
            .withDefault({ it }, "127.0.0.1")
        identityDbTemplatePostgresPort = "IDENTITYDB_TEMPLATE_POSTGRES_PORT"
            .optionalEnvVar()
            .withDefault({ Integer.parseInt(it) }, 5432)
        identityDbTemplatePostgresDb = "IDENTITYDB_TEMPLATE_POSTGRES_DB".optionalEnvVar()
        identityDbTemplatePostgresUser = "IDENTITYDB_TEMPLATE_POSTGRES_USER".optionalEnvVar()
        identityDbTemplatePostgresPassword = "IDENTITYDB_TEMPLATE_POSTGRES_PASSWORD".optionalEnvVar()
        identityDbTemplateCitizenTable = "IDENTITYDB_TEMPLATE_CITIZEN_TABLE".requireEnvVar()
        maxDids = "MAX_DIDS".requireEnvVar().toInt()
        hashPepper = "HASH_PEPPER".requireEnvVar()
    }

    /**
     * Provided the name of an environment variable, retrieve it if it exists, or else throw an exception.
     */
    private final fun String.requireEnvVar(): String {
        return dotenv[this] ?: throw MissingEnvVarException(this)
    }

    /**
     * Provided the name of an environment variable, retrieve it if it exists, or else return null.
     */
    private final fun String.optionalEnvVar(): String? {
        return dotenv[this]
    }

    /**
     * Useful function for converting an optional string to a value (with a default value)
     */
    private final fun <T> String?.withDefault(onDefined: (str: String) -> T, default: T): T {
        return this?.let { onDefined(it) } ?: run { default }
    }
}
