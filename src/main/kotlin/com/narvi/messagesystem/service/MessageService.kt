package com.narvi.messagesystem.service

import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.kafka.MessageNotificationRecord
import com.narvi.messagesystem.dto.websocket.outbound.MessageNotification
import com.narvi.messagesystem.json.JsonUtil
import com.narvi.messagesystem.session.WebSocketSessionManager
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.function.Consumer

@Service
class MessageService(
    private val pushService: PushService,
    private val webSocketSessionManager: WebSocketSessionManager,
    private val jsonUtil: JsonUtil,
) {

    init {
        pushService.registerPushMessageType(MessageType.NOTIFY_MESSAGE, MessageNotificationRecord::class.java)
    }

    private val senderThreadPool: ExecutorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE)

    fun sendMessage(
        record: MessageNotificationRecord,
    ) {
        val messageSender: Consumer<UserId> = Consumer { participantId ->
            val participantSession = webSocketSessionManager.getSession(participantId)
            val messageNotification = MessageNotification(
                channelId = record.channelId,
                messageSeqId = record.messageSeqId,
                username = record.username,
                content = record.content
            )

            if (participantSession != null) {
                jsonUtil.toJson(messageNotification)?.let { json ->
                    try {
                        webSocketSessionManager.sendMessage(participantSession, json)
                    } catch (ex: Exception) {
                        pushService.pushMessage(record)
                    }
                }
            } else {
                pushService.pushMessage(record)
            }
        }

        record.participantIds.forEach { participantId ->
            CompletableFuture.runAsync({
                messageSender.accept(participantId)
            }, senderThreadPool)
        }
    }

    companion object {
        private const val THREAD_POOL_SIZE = 10
    }
}