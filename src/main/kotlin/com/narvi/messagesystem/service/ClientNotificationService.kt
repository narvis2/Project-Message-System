package com.narvi.messagesystem.service

import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.outbound.BaseMessage
import com.narvi.messagesystem.json.JsonUtil
import com.narvi.messagesystem.session.WebSocketSessionManager
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession

@Service
class ClientNotificationService(
    private val webSocketSessionManager: WebSocketSessionManager,
    private val pushService: PushService,
    private val jsonUtil: JsonUtil,
) {

    init {
        // 클라이언트에 푸시 가능한 메시지 타입 등록
        listOf(
            MessageType.INVITE_RESPONSE,
            MessageType.ASK_INVITE,
            MessageType.ACCEPT_RESPONSE,
            MessageType.NOTIFY_ACCEPT,
            MessageType.JOIN_RESPONSE,
            MessageType.NOTIFY_JOIN,
            MessageType.DISCONNECT_RESPONSE,
            MessageType.REJECT_RESPONSE,
            MessageType.CREATE_RESPONSE,
            MessageType.QUIT_RESPONSE
        ).forEach { pushService.registerPushMessageType(it) }
    }

    // 명시된 세션을 통해 메시지를 전송합니다.
    fun sendMessage(session: WebSocketSession, userId: UserId, baseMessage: BaseMessage) {
        sendPayload(session, userId, baseMessage)
    }

    /**
     * userId에 매핑된 세션이 존재하면 해당 세션으로,
     * 없으면 푸시 서비스로 메시지를 전송합니다.
     */
    fun sendMessage(userId: UserId, message: BaseMessage) {
        val session = webSocketSessionManager.getSession(userId)
        sendPayload(session, userId, message)
    }

    /**
     * 실질적인 메시지 전송 로직.
     * JSON 직렬화 실패 시 에러 로그 출력 및 푸시 전송.
     */
    private fun sendPayload(session: WebSocketSession?, userId: UserId, message: BaseMessage) {
        val json = jsonUtil.toJson(message)

        if (json == null) {
            log.error("Send message failed. MessageType: {}", message.type)
            return
        }

        try {
            session?.let {
                webSocketSessionManager.sendMessage(it, json)
                return
            } ?: kotlin.run {
                pushService.pushMessage(userId, message.type, json)
            }
        } catch (ex: Exception) {
            pushService.pushMessage(userId, message.type, json)
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}