package org.kiva.identityservice.utils

import org.springframework.beans.factory.config.YamlProcessor
import org.springframework.core.io.Resource

open class YamlLoader(vararg resources: Resource) : YamlProcessor() {

    private var map: Map<String, Any>? = null

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
                output[key] = value
            }
        }
    }
}
