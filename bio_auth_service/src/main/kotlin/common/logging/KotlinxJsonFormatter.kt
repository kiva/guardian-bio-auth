package org.kiva.bioauthservice.common.logging

import ch.qos.logback.contrib.json.JsonFormatter
import ch.qos.logback.contrib.json.classic.JsonLayout
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class KotlinxJsonFormatter : JsonFormatter {

    @Serializable
    internal data class Log(
        val timestamp: String,
        val level: String,
        val thread: String,
        val logger: String,
        val message: String,
        val exception: String? = null
    )

    override fun toJsonString(map: MutableMap<*, *>): String {
        val log = Log(
            timestamp = map[JsonLayout.TIMESTAMP_ATTR_NAME]?.toString() ?: "",
            level = map[JsonLayout.LEVEL_ATTR_NAME]?.toString() ?: "",
            thread = map[JsonLayout.THREAD_ATTR_NAME]?.toString() ?: "",
            logger = map[JsonLayout.LOGGER_ATTR_NAME]?.toString() ?: "",
            message = map[JsonLayout.FORMATTED_MESSAGE_ATTR_NAME]?.toString() ?: "",
            exception = map[JsonLayout.EXCEPTION_ATTR_NAME]?.toString()
        )
        val stringifiedLog = Json.encodeToString(Log.serializer(), log)
        return "$stringifiedLog\n"
    }
}
