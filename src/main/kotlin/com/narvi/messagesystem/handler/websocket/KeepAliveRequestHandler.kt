package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.Constants
import com.narvi.messagesystem.dto.websocket.inbound.KeepAliveRequest
import com.narvi.messagesystem.service.SessionService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class KeepAliveRequestHandler(
    private val sessionService: SessionService,
) : BaseRequestHandler<KeepAliveRequest> {
    override fun handleRequest(senderSession: WebSocketSession, request: KeepAliveRequest) {
        (senderSession.attributes[Constants.HTTP_SESSION_ID.value] as? String)?.let {
            sessionService.refreshTTL(it)
        }
    }
}