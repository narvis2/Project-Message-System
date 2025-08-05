package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.inbound.AcceptRequest
import com.narvi.messagesystem.dto.websocket.outbound.AcceptNotification
import com.narvi.messagesystem.dto.websocket.outbound.AcceptResponse
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.service.UserConnectionService
import com.narvi.messagesystem.session.WebSocketSessionManager
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class AcceptRequestHandler(
    private val webSocketSessionManager: WebSocketSessionManager,
    private val userConnectionService: UserConnectionService,
) : BaseRequestHandler<AcceptRequest> {
    override fun handleRequest(senderSession: WebSocketSession, request: AcceptRequest) {
        val acceptorUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId

        val (invitorUserId, errorMessage) = userConnectionService.accept(acceptorUserId, request.username)

        if (invitorUserId != null) {
            webSocketSessionManager.sendMessage(senderSession, AcceptResponse(request.username))
            webSocketSessionManager.getSession(invitorUserId)?.let {
                webSocketSessionManager.sendMessage(it, AcceptNotification(errorMessage))
            }
        } else {
            webSocketSessionManager.sendMessage(senderSession, ErrorResponse(MessageType.ACCEPT_REQUEST, errorMessage))
        }
    }
}