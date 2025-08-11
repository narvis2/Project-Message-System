package com.narvi.messagesystem.config

import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate

@Configuration
class KafkaConfig {

    @Bean
    fun kafkaTemplate(
        producerFactory: DefaultKafkaProducerFactory<String, String>
    ): KafkaTemplate<String, String> {
        val producer = producerFactory.createProducer()
        producer.close()
        log.info("Kafka producer initialize.")
        return KafkaTemplate(producerFactory)
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}