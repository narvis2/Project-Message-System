package com.narvi.messagesystem.kafka

import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.CreateTopicsResult
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.errors.TopicExistsException
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.stereotype.Service
import java.util.concurrent.ExecutionException

@Service
class ListenTopicCreator(
    private val kafkaAdmin: KafkaAdmin,
    @Value("\${message-system.kafka.listeners.listen.prefix-topic}")
    val prefixListenTopic: String,
    @Value("\${message-system.kafka.listeners.listen.prefix-group-id}")
    val prefixGroupId: String,
    @Value("\${message-system.kafka.listeners.listen.partitions}")
    val partitions: String,
    @Value("\${message-system.kafka.listeners.listen.replicationFactor}")
    val replicationFactor: String,
    @Value("\${server.id}")
    val serverId: String,
) {

    // Bean 이 만들어지는 다음에 초기화
    @PostConstruct
    fun init() {
        createTopic(getListenTopic(), partitions.toInt(), replicationFactor.toShort())
    }

    fun createTopic(topicName: String, partitions: Int, replicationFactor: Short) {
        AdminClient.create(kafkaAdmin.configurationProperties).use { adminClient ->
            val newTopic = NewTopic(topicName, partitions, replicationFactor)
            val topicsResult: CreateTopicsResult = adminClient.createTopics(listOf(newTopic))

            topicsResult.values().forEach { (_, future) ->
                try {
                    future.get()
                    log.info("Create topic: {}", topicName)
                } catch (ex: ExecutionException) {
                    if (ex.cause is TopicExistsException) {
                        log.info("Already existing topic: {}", topicName)
                    } else {
                        val message = "Create topic failed. topic: $topicName, cause: ${ex.message}"
                        log.error(message)
                        throw RuntimeException(message, ex)
                    }
                } catch (ex: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw RuntimeException("Interrupt", ex)
                }
            }
        }
    }

    fun getListenTopic(): String = "$prefixListenTopic-$serverId"

    fun getConsumerGroupId(): String = "$prefixGroupId-$serverId"

    companion object {
        private val log = KotlinLogging.logger { }

    }
}