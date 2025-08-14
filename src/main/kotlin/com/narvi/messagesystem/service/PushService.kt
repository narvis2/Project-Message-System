package com.narvi.messagesystem.service

import com.narvi.messagesystem.dto.kafka.BaseRecord
import com.narvi.messagesystem.kafka.KafkaProducer
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class PushService(
    private val kafkaProducer: KafkaProducer
) {
    // FCM Push 가 필요한 EVENT 만 처리
    private val pushMessageTypes = HashMap<String, Class<out BaseRecord>>()

    fun registerPushMessageType(messageType: String, clazz: Class<out BaseRecord>) {
        pushMessageTypes[messageType] = clazz
    }

    fun pushMessage(record: BaseRecord) {
        val messageType = record.type
        if (pushMessageTypes.containsKey(messageType)) {
            kafkaProducer.sendPushNotification(record)
        } else {
            log.error("Invalid message type: {}", messageType)
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}