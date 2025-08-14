package com.narvi.messagesystem.kafka

import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.kafka.BaseRecord
import com.narvi.messagesystem.json.JsonUtil
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import java.util.function.BiConsumer

@Service
class KafkaProducer(
    private val jsonUtil: JsonUtil,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    @Value("\${message-system.kafka.topics.push}")
    private val pushTopic: String,
    @Value("\${message-system.kafka.topics.message}")
    private val messageTopic: String,
    @Value("\${message-system.kafka.topics.request}")
    private val requestTopic: String
) {

    fun sendRequest(baseRecord: BaseRecord, errorCallback: (() -> Unit)?) {
        jsonUtil.toJson(baseRecord)?.let { record ->
            kafkaTemplate.send(requestTopic, record).whenComplete(logResult(requestTopic, record, null, errorCallback))
        }
    }

    fun sendMessageUsingPartitionKey(
        channelId: ChannelId,
        userId: UserId,
        baseRecord: BaseRecord,
        errorCallback: (() -> Unit)?
    ) {
        val partitionKey = "${channelId.id}-${userId.id}"

        jsonUtil.toJson(baseRecord)?.let { record ->
            kafkaTemplate.send(messageTopic, partitionKey, record).whenComplete(logResult(messageTopic, record, partitionKey, errorCallback))
        }
    }

    fun sendPushNotification(baseRecord: BaseRecord) {
        jsonUtil.toJson(baseRecord)?.let { record ->
            kafkaTemplate.send(pushTopic, record).whenComplete(logResult(pushTopic, record, null, null))
        }
    }

    private fun logResult(
        topic: String,
        record: String,
        partitionKey: String?,
        errorCallback: (() -> Unit)?
    ): BiConsumer<SendResult<String, String>, Throwable?> =
        BiConsumer { sendResult, throwable ->
            if (throwable == null) {
                log.info(
                    "Record produced: {} with key: {} to topic: {}",
                    sendResult.producerRecord.value(),
                    partitionKey,
                    sendResult.producerRecord.topic()
                )
            } else {
                log.error(
                    "Record producing failed: {} with key: {} to topic: {}, cause: {}",
                    record,
                    partitionKey,
                    topic,
                    throwable.message
                )
                errorCallback?.invoke()
            }
        }


    companion object {
        private val log = KotlinLogging.logger {}
    }
}