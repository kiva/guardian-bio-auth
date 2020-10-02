package org.kiva.identityservice.utils

import org.springframework.beans.factory.config.YamlProcessor
import org.springframework.core.io.Resource

open class YamlLoader(vararg resources: Resource) : YamlProcessor() {

    private var map: Map<String, Any>? = null

    /*
    If the config value starts with ENV_CONFIG_PREFIX, it means that the value is a reference to environmental variables.
    For instance, if a config value is defined as ENV:$ENV_POSTGRES_PASS the actual config value would be System.getEnv(ENV_POSTGRES_PASS).
    */
    private val ENV_CONFIG_PREFIX = "ENV:$"

    init {
        setResources(*resources)
    }

    fun load(): Map<String, Any>? {
        if (map == null) {
            this.map = createMap()
        }
        return this.map
    }

    private fun createMap(): Map<String, Any> {
        val result = LinkedHashMap<String, Any>()
        process { _, map -> merge(result, map) }
        return result
    }

    private fun merge(output: MutableMap<String, Any>, map: Map<String, Any>) {
        map.forEach { (key, value) ->
            val existing = output[key]
            if (value is Map<*, *>) {
                // Inner cast required by Eclipse IDE.
                val result = if (existing is Map<*, *>) LinkedHashMap(existing as Map<String, Any>) else LinkedHashMap(emptyMap<String, Any>())
                merge(result, value as Map<String, Any>)
                output[key] = result
            } else {
                if (value.toString().startsWith(ENV_CONFIG_PREFIX)) {
                    val valueStr = value.toString()
                    // The prefix is ignored getting the environment variable value.
                    val envVariableName = valueStr.drop(ENV_CONFIG_PREFIX.length)
                    output[key] = if (System.getenv(envVariableName).isNullOrEmpty()) envVariableName else System.getenv(envVariableName)
                } else {
                    output[key] = value
                }
            }
        }
    }
}
