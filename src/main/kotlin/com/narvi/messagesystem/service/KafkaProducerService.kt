package com.narvi.messagesystem.service

import com.narvi.messagesystem.dto.kafka.outbound.BaseRecord
import com.narvi.messagesystem.json.JsonUtil
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaProducerService(
    private val jsonUtil: JsonUtil,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    @Value("\${message-system.kafka.listeners.push.topic}")
    private val pushTopic: String,
) {

    fun sendPushNotification(baseRecord: BaseRecord) {
        jsonUtil.toJson(baseRecord)?.let { record ->
            kafkaTemplate.send(pushTopic, record).whenComplete { sendResult, throwable ->
                if (throwable == null) {
                    log.info(
                        "Record produced: {} to topic: {}",
                        sendResult.producerRecord.value(),
                        sendResult.producerRecord.topic()
                    )
                } else {
                    log.error("Record producing failed: {} to topic: {}, cause: {}", record, pushTopic, throwable.message)
                }
            }
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}