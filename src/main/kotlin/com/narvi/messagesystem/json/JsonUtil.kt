package com.narvi.messagesystem.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class JsonUtil {
    private val objectMapper = ObjectMapper().apply {
        registerModules(JavaTimeModule())
        registerModules(kotlinModule())
        registerModule(ParameterNamesModule())
        activateDefaultTyping(
            BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Any::class.java)
                .build(),
            ObjectMapper.DefaultTyping.NON_FINAL,
        )
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    fun <T> fromJson(json: String, clazz: Class<T>): T? = runCatching {
        objectMapper.readValue(json, clazz)
    }.getOrElse {
        log.error("Failed JSON to Object: ${it.message}")
        null
    }

    fun <T> toJson(obj: T): String? = runCatching {
        objectMapper.writeValueAsString(obj)
    }.getOrElse {
        log.error("Failed Object to JSON: ${it.message}")
        null
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}