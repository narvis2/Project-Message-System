package com.narvi.messagesystem.session

import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.outbound.BaseMessage
import com.narvi.messagesystem.json.JsonUtil
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

@Component
class WebSocketSessionManager(
    private val jsonUtil: JsonUtil,
) {

    private val sessions: MutableMap<UserId, WebSocketSession> = ConcurrentHashMap<UserId, WebSocketSession>()

    fun getSessions(): List<WebSocketSession> = sessions.values.toList()

    fun getSession(userId: UserId) = sessions[userId]

    fun putSession(userId: UserId, session: WebSocketSession) {
        log.info("Store Session : {}", session.id)
        sessions[userId] = session
    }

    fun closeSession(userId: UserId) {
        try {
            sessions.remove(userId)?.let {
                log.info("Remove session : {}", userId)
                it.close()
                log.info("Closed session : {}", userId)
            }
        } catch (e: Exception) {
            log.error("Failed WebSocketSession close. userId : {}", userId)
        }
    }

    fun sendMessage(session: WebSocketSession, message: BaseMessage) {
        jsonUtil.toJson(message)?.let {
            try {
                session.sendMessage(TextMessage(it))
                log.info("send message: {} to {}", it, session.id)
            } catch (e: Exception) {
                log.error("메시지 전송 실패. cause: {}", e.message)
            }
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}