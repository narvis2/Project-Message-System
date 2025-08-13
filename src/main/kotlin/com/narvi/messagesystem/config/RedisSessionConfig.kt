package com.narvi.messagesystem.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.narvi.messagesystem.constant.KeyPrefix
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.security.jackson2.SecurityJackson2Modules
import org.springframework.session.FlushMode
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession

@Configuration
@EnableRedisHttpSession(
    redisNamespace = KeyPrefix.USER_SESSION,
    maxInactiveIntervalInSeconds = 300,
    flushMode = FlushMode.IMMEDIATE // SessionRepository 에서 수정할때 바로 적용되도록
)
class RedisSessionConfig {

    @Bean
    fun springSessionDefaultRedisSerializer(): RedisSerializer<Any> {
//        val objectMapper = ObjectMapper().apply {
//            registerModule(JavaTimeModule())
//            registerModules(kotlinModule())
//            registerModule(ParameterNamesModule())
//            registerModules(SecurityJackson2Modules.getModules(this.javaClass.classLoader))
//            activateDefaultTyping(
//                BasicPolymorphicTypeValidator.builder()
//                    .allowIfSubType(Any::class.java)
//                    .build(),
//                ObjectMapper.DefaultTyping.NON_FINAL,
//            )
//                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//        }

        return GenericJackson2JsonRedisSerializer(redisCacheObjectMapper())
    }

    private fun redisCacheObjectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper
            .registerModule(JavaTimeModule())
            .registerModule(ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
            .registerModules(SecurityJackson2Modules.getModules(this.javaClass.classLoader))
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .activateDefaultTyping(
                objectMapper.polymorphicTypeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
            )
        GenericJackson2JsonRedisSerializer.registerNullValueSerializer(objectMapper, null)
        return objectMapper
    }
}