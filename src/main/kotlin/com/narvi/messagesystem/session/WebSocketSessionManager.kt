package com.narvi.messagesystem.session

import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

@Component
class WebSocketSessionManager {

    private val sessions: MutableMap<String, WebSocketSession> = ConcurrentHashMap<String, WebSocketSession>()

    fun getSessions(): List<WebSocketSession> = sessions.values.toList()

    fun storeSession(session: WebSocketSession) {
        log.info("Store Session : {}", session.id)
        sessions[session.id] = session
    }

    fun terminateSession(sessionId: String) {
        try {
            sessions.remove(sessionId)?.let {
                log.info("Remove session : {}", sessionId)
                it.close()
                log.info("Closed session : {}", sessionId)
            }
        } catch (e: Exception) {
            log.error("Failed WebSocketSession close. sessionId : {}", sessionId)
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}