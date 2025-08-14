package com.narvi.messagesystem.service

import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.kafka.*
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
        hashMapOf(
            MessageType.INVITE_RESPONSE to InviteResponseRecord::class.java,
            MessageType.ASK_INVITE to InviteNotificationRecord::class.java,
            MessageType.ACCEPT_RESPONSE to AcceptResponseRecord::class.java,
            MessageType.NOTIFY_ACCEPT to AcceptNotificationRecord::class.java,
            MessageType.NOTIFY_JOIN to JoinNotificationRecord::class.java,
            MessageType.DISCONNECT_RESPONSE to DisconnectResponseRecord::class.java,
            MessageType.REJECT_RESPONSE to RejectResponseRecord::class.java,
            MessageType.CREATE_RESPONSE to CreateResponseRecord::class.java,
            MessageType.QUIT_RESPONSE to QuitResponseRecord::class.java,
        ).forEach {
            pushService.registerPushMessageType(it.key, it.value)
        }
    }

    // 명시된 세션을 통해 메시지를 전송합니다.
    fun sendErrorMessage(session: WebSocketSession, baseMessage: BaseMessage) {
        sendPayload(session, baseMessage, null)
    }

    /**
     * userId에 매핑된 세션이 존재하면 해당 세션으로,
     * 없으면 푸시 서비스로 메시지를 전송합니다.
     */
    fun sendMessage(userId: UserId, message: BaseMessage, record: BaseRecord?) {
        val session = webSocketSessionManager.getSession(userId)
        sendPayload(session, message, record)
    }

    /**
     * 실질적인 메시지 전송 로직.
     * JSON 직렬화 실패 시 에러 로그 출력 및 푸시 전송.
     */
    private fun sendPayload(session: WebSocketSession?, message: BaseMessage, record: BaseRecord?) {
        val json = jsonUtil.toJson(message)

        if (json == null) {
            log.error("Send message failed. MessageType: {}", message.type)
            return
        }

        val pushMessage: () -> Unit = {
            record?.let(pushService::pushMessage)
        }

        try {
            session?.let {
                webSocketSessionManager.sendMessage(it, json)
                return
            } ?: kotlin.run {
                pushMessage.invoke()
            }
        } catch (ex: Exception) {
            pushMessage.invoke()
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}