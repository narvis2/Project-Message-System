package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.inbound.AcceptRequest
import com.narvi.messagesystem.dto.websocket.outbound.AcceptNotification
import com.narvi.messagesystem.dto.websocket.outbound.AcceptResponse
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.service.ClientNotificationService
import com.narvi.messagesystem.service.UserConnectionService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class AcceptRequestHandler(
    private val clientNotificationService: ClientNotificationService,
    private val userConnectionService: UserConnectionService,
) : BaseRequestHandler<AcceptRequest> {
    override fun handleRequest(senderSession: WebSocketSession, request: AcceptRequest) {
        val acceptorUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId

        val (invitorUserId, errorMessage) = userConnectionService.accept(acceptorUserId, request.username)

        if (invitorUserId != null) {
            clientNotificationService.sendMessage(senderSession, acceptorUserId, AcceptResponse(request.username))
            clientNotificationService.sendMessage(invitorUserId, AcceptNotification(errorMessage))
        } else {
            clientNotificationService.sendMessage(
                senderSession,
                acceptorUserId,
                ErrorResponse(MessageType.ACCEPT_REQUEST, errorMessage)
            )
        }
    }
}