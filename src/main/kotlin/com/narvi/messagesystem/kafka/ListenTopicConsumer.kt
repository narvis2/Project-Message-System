package com.narvi.messagesystem.kafka

import com.narvi.messagesystem.dto.kafka.BaseRecord
import com.narvi.messagesystem.handler.kafka.RecordDispatcher
import com.narvi.messagesystem.json.JsonUtil
import mu.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service

@Service
class ListenTopicConsumer(
    private val listenTopicCreator: ListenTopicCreator,
    private val recordDispatcher: RecordDispatcher,
    private val jsonUtil: JsonUtil,
) {
    @KafkaListener(
        topics = ["#{__listener.getListenTopic()}"],
        groupId = "#{__listener.getConsumerGroupId()}",
        concurrency = "\${message-system.kafka.listeners.listen.concurrency}"
    )
    fun listenTopicConsumerGroup(
        consumerRecord: ConsumerRecord<String, String>,
        acknowledgment: Acknowledgment,
    ) {
        log.info(
            "Received record: {}, from topic: {}, on key: {}, partition: {}, offset: {}",
            consumerRecord.value(),
            consumerRecord.topic(),
            consumerRecord.key(),
            consumerRecord.partition(),
            consumerRecord.offset()
        )

        jsonUtil.fromJson(consumerRecord.value(), BaseRecord::class.java)
            ?.let(recordDispatcher::dispatchRecord)
            ?: run {
                log.error("Record dispatch failed. record: {}", consumerRecord.value())
            }

        acknowledgment.acknowledge() // Consumer Commit
    }

    fun getListenTopic(): String = listenTopicCreator.getListenTopic()

    fun getConsumerGroupId(): String = listenTopicCreator.getConsumerGroupId()

    companion object {
        private val log = KotlinLogging.logger { }
    }
}