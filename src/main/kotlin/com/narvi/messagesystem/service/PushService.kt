package com.narvi.messagesystem.service

import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.kafka.outbound.BaseRecord
import com.narvi.messagesystem.json.JsonUtil
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class PushService(
    private val jsonUtil: JsonUtil,
    private val kafkaProducerService: KafkaProducerService
) {
    // FCM Push 가 필요한 EVENT 만 처리
    private val pushMessageTypes = HashMap<String, Class<out BaseRecord>>()

    fun registerPushMessageType(messageType: String, clazz: Class<out BaseRecord>) {
        pushMessageTypes[messageType] = clazz
    }

    fun pushMessage(userId: UserId, messageType: String, message: String) {
        val baseRecord = pushMessageTypes[messageType]
        if (baseRecord != null) {
            jsonUtil.addValue(message, "userId", userId.id.toString())?.let { json ->
                jsonUtil.fromJson(json, baseRecord)?.let(kafkaProducerService::sendPushNotification)
            }

            log.info("push message: {} to user: {}", message, userId)
        } else {
            log.error("Invalid message type: {}", messageType)
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}