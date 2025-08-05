package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.inbound.KeepAlive
import com.narvi.messagesystem.service.SessionService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class KeepAliveHandler(
    private val sessionService: SessionService,
) : BaseRequestHandler<KeepAlive> {
    override fun handleRequest(senderSession: WebSocketSession, request: KeepAlive) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as? UserId ?: return
        val httpSessionId = senderSession.attributes[IdKey.HTTP_SESSION_ID.value] as? String ?: return

        sessionService.refreshTTL(senderUserId, httpSessionId)
    }
}