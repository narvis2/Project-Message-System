package com.narvi.messagesystem.config

import com.narvi.messagesystem.kafka.KafkaConsumerAwareRebalanceListener
import mu.KotlinLogging
import org.apache.kafka.clients.admin.AdminClientConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.ContainerProperties

@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String
) {

    // 자신만의 수신 전용 Topic
    @Bean
    fun kafkaAdmin(): KafkaAdmin {
        val configs: MutableMap<String, Any> = HashMap()
        configs["bootstrap.servers"] = bootstrapServers
        configs[AdminClientConfig.RETRIES_CONFIG] = 5
        configs[AdminClientConfig.RETRY_BACKOFF_MS_CONFIG] = 1000
        return KafkaAdmin(configs)
    }

    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, String>,
        awareRebalanceListener: KafkaConsumerAwareRebalanceListener,
    ): ConcurrentKafkaListenerContainerFactory<String, String> {
        val containerFactory = ConcurrentKafkaListenerContainerFactory<String, String>()
        containerFactory.consumerFactory = consumerFactory
        // Consumer 가 메시지를 읽은 후 오프셋을 언제 커밋할지를 결정하는 설정
        containerFactory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        containerFactory.containerProperties.setConsumerRebalanceListener(awareRebalanceListener)

        log.info("🚦 Set AckMode: {}", containerFactory.containerProperties.ackMode)
        return containerFactory
    }

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