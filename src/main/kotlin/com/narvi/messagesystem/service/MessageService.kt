package com.narvi.messagesystem.service

import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.constant.ResultType
import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.Message
import com.narvi.messagesystem.dto.domain.MessageSeqId
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.kafka.outbound.MessageNotificationRecord
import com.narvi.messagesystem.dto.websocket.outbound.BaseMessage
import com.narvi.messagesystem.dto.websocket.outbound.WriteMessageAck
import com.narvi.messagesystem.json.JsonUtil
import com.narvi.messagesystem.repository.UserChannelRepository
import com.narvi.messagesystem.session.WebSocketSessionManager
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Service
class MessageService(
    private val channelService: ChannelService,
    private val pushService: PushService,
    private val webSocketSessionManager: WebSocketSessionManager,
    private val jsonUtil: JsonUtil,
    private val userChannelRepository: UserChannelRepository,
    private val userService: UserService,
    private val messageShardService: MessageShardService
) {

    init {
        pushService.registerPushMessageType(MessageType.NOTIFY_MESSAGE, MessageNotificationRecord::class.java)
    }

    private val senderThreadPool: ExecutorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE)

    @Transactional(readOnly = true)
    fun getMessages(
        channelId: ChannelId,
        startMessageSeqId: MessageSeqId,
        endMessageSeqId: MessageSeqId
    ): Pair<List<Message>, ResultType> {
        val messageInfos = messageShardService.findByChannelIdAndMessageSequenceBetween(
            channelId,
            startMessageSeqId,
            endMessageSeqId
        )
        val userIds = messageInfos.map { UserId(it.userId) }.toSet()

        if (userIds.isEmpty()) {
            return emptyList<Message>() to ResultType.SUCCESS
        }

        val result = userService.getUsernames(userIds)
        return if (result.second == ResultType.SUCCESS) {
            val messages = messageInfos.map { projection ->
                val userId = UserId(projection.userId)

                Message(
                    channelId = channelId,
                    messageSeqId = MessageSeqId(projection.messageSequence),
                    username = result.first.getOrDefault(userId, "unknown"), // unknown 👉 탈퇴한 유저인 경우
                    content = projection.content
                )
            }

            messages to result.second
        } else {
            emptyList<Message>() to result.second
        }
    }

    @Transactional
    fun sendMessage(
        senderUserId: UserId,
        content: String,
        channelId: ChannelId,
        messageSeqId: MessageSeqId,
        serial: Long,
        message: BaseMessage,
    ) {
        val payload = jsonUtil.toJson(message)

        if (payload == null) {
            log.error("Send message failed. MessageType: {}", message.type)
            return
        }

        try {
            messageShardService.save(
                channelId,
                messageSeqId,
                senderUserId,
                content
            )
        } catch (ex: Exception) {
            log.error("Send message failed. cause: {}", ex.message)
            return
        }

        val allParticipantIds = channelService.getParticipantIds(channelId)
        val onlineParticipantIds = channelService.getOnlineParticipantIds(channelId, allParticipantIds)

        allParticipantIds.forEach { participantId ->
            // 자기 자신한테는 메시지를 보내지 않음
            if (participantId == senderUserId) {
                // 자신이 읽은 메시지 읽었다고 처리
                updateLastReadMsgSeq(senderUserId, channelId, messageSeqId)
                jsonUtil.toJson(WriteMessageAck(serial, messageSeqId))?.let { writeMessageAck ->
                    CompletableFuture.runAsync({
                        try {
                            val senderSession = webSocketSessionManager.getSession(senderUserId)
                            if (senderSession != null) {
                                webSocketSessionManager.sendMessage(senderSession, writeMessageAck)
                            }
                        } catch (ex: Exception) {
                            log.error("Send writeMessageAck failed. userId: {}, cause: {}", senderUserId.id, ex.message)
                        }
                    }, senderThreadPool)
                }
                return@forEach
            }

            val isOnline = participantId in onlineParticipantIds

            /**
             * Online 의 경우 WebSocket 으로 Message
             * Offline 의 경우 Kafka -> FCM Push Notification
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

    @Transactional
    fun updateLastReadMsgSeq(userId: UserId, channelId: ChannelId, messageSeqId: MessageSeqId) {
        if (userChannelRepository.updateLastReadMsgSeqByUserIdAndChannelId(
                userId.id,
                channelId.id,
                messageSeqId.id
            ) == 0
        ) {
            log.error(
                "Update lastReadMsgSeq failed. No record found for UserId: {} and ChannelId: {}",
                userId.id,
                channelId.id
            )
        }
    }

    companion object {
        private const val THREAD_POOL_SIZE = 10
        private val log = KotlinLogging.logger {}
    }
}