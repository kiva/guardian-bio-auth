package org.kiva.identityservice.config

import org.kiva.identityservice.StringToEnumConverterFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.ConverterRegistry

@Configuration
class Config {

    @Bean
    fun stringToEnumConverterFactory(registry: ConverterRegistry): StringToEnumConverterFactory {
        val converterFactory = StringToEnumConverterFactory()
        registry.addConverterFactory(converterFactory)
        return converterFactory
    }
}
