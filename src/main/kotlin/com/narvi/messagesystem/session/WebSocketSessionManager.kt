package com.narvi.messagesystem.session

import com.narvi.messagesystem.dto.domain.UserId
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

@Component
class WebSocketSessionManager {

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

    fun sendMessage(session: WebSocketSession, message: String) {
        try {
            session.sendMessage(TextMessage(message))
            log.info("send message: {} to {}", message, session.id)
        } catch (e: IOException) {
            log.error("Send message failed. cause: {}", e.message)
            throw e
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}