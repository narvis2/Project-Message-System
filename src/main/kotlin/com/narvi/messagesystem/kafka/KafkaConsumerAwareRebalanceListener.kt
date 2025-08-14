package com.narvi.messagesystem.kafka

import mu.KotlinLogging
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.common.TopicPartition
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener
import org.springframework.stereotype.Component

@Component
class KafkaConsumerAwareRebalanceListener : ConsumerAwareRebalanceListener {


    // Consumer 가 할당될떄 호출됨
    override fun onPartitionsAssigned(consumer: Consumer<*, *>, partitions: MutableCollection<TopicPartition>) {
        log.info("Kafka consumer {} assigned: {}", consumer, partitions.toString())
    }

    // Consumer 가 회수될때 호출됨
    override fun onPartitionsRevoked(partitions: MutableCollection<TopicPartition>) {
        log.info("Kafka consumer revoked: {}", partitions.toString())
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}