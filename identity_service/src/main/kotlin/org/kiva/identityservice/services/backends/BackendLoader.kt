package org.kiva.identityservice.services.backends

import java.io.IOException
import java.util.stream.Collectors
import org.kiva.identityservice.utils.YamlLoader
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

/**
 * Loads backend definition from yaml configuration file.
 */
@Configuration
@PropertySource("classpath:/backends.yml")
class BackendLoader(val context: ApplicationContext) : IBackendLoader {

    @Throws(IOException::class)
    override fun load(): List<Definition> {
        return try {
            val loader = YamlLoader(*context.getResources("classpath:/backends.yml"))

            loader.load()?.let {
                it.entries.stream()
                        .map { config -> Definition(config.key, config.value as Map<String, Any>) }
                        .collect(Collectors.toList())
            } ?: listOf()
        } catch (ex: Exception) {
            logger.error("Error during yml load", ex)
            listOf()
        }
    }

    private val logger = LoggerFactory.getLogger(javaClass)
}
