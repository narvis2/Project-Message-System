package com.narvi.messagesystem.service

import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.outbound.BaseMessage
import com.narvi.messagesystem.entity.MessageEntity
import com.narvi.messagesystem.json.JsonUtil
import com.narvi.messagesystem.repository.MessageRepository
import com.narvi.messagesystem.session.WebSocketSessionManager
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Service
class MessageService(
    private val messageRepository: MessageRepository,
    private val channelService: ChannelService,
    private val pushService: PushService,
    private val webSocketSessionManager: WebSocketSessionManager,
    private val jsonUtil: JsonUtil,
) {

    init {
        pushService.registerPushMessageType(MessageType.NOTIFY_MESSAGE)
    }

    private val senderThreadPool: ExecutorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE)

    fun sendMessage(
        senderUserId: UserId,
        content: String,
        channelId: ChannelId,
        message: BaseMessage,
    ) {
        val payload = jsonUtil.toJson(message)

        if (payload == null) {
            log.error("Send message failed. MessageType: {}", message.type)
            return
        }

        try {
            messageRepository.save(
                MessageEntity(
                    userId = senderUserId.id, content = content
                )
            )
        } catch (ex: Exception) {
            log.error("Send message failed. cause: {}", ex.message)
            return
        }

        val allParticipantIds = channelService.getParticipantIds(channelId)
        val onlineParticipantIds = channelService.getOnlineParticipantIds(channelId, allParticipantIds)

        allParticipantIds.forEachIndexed { idx, participantId ->
            if (participantId == senderUserId) return@forEachIndexed

            val isOnline = onlineParticipantIds.getOrNull(idx) != null
            /**
             * Online 의 경우 WebSocket 으로 Message
             * Offline 의 경우 FCM Push Notification
             */
            if (isOnline) {
                CompletableFuture.runAsync({
                    try {
                        val session = webSocketSessionManager.getSession(participantId)
                        if (session != null) {
                            webSocketSessionManager.sendMessage(session, payload)
                        } else {
                            pushService.pushMessage(participantId, MessageType.NOTIFY_MESSAGE, payload)
                        }
                    } catch (ex: Exception) {
                        // 소켓에 문제가 있어서 예외가 터진 경우
                        pushService.pushMessage(participantId, MessageType.NOTIFY_MESSAGE, payload)
                    }
                }, senderThreadPool)
            } else {
                pushService.pushMessage(participantId, MessageType.NOTIFY_MESSAGE, payload)
            }
        }
    }

    companion object {
        private const val THREAD_POOL_SIZE = 10
        private val log = KotlinLogging.logger {}
    }
}