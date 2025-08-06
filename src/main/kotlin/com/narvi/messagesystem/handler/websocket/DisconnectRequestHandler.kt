package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.constant.UserConnectionStatus
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.inbound.DisconnectRequest
import com.narvi.messagesystem.dto.websocket.outbound.DisconnectResponse
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.service.ClientNotificationService
import com.narvi.messagesystem.service.UserConnectionService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class DisconnectRequestHandler(
    private val clientNotificationService: ClientNotificationService,
    private val userConnectionService: UserConnectionService,
) : BaseRequestHandler<DisconnectRequest> {
    override fun handleRequest(senderSession: WebSocketSession, request: DisconnectRequest) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId

        val result = userConnectionService.disconnect(senderUserId, request.username)
        val isSuccess = result.first

        if (isSuccess) {
            clientNotificationService.sendMessage(
                senderSession, senderUserId, DisconnectResponse(
                    request.username, UserConnectionStatus.DISCONNECTED
                )
            )
        } else {
            val errorMessage = result.second
            clientNotificationService.sendMessage(
                senderSession, senderUserId, ErrorResponse(MessageType.DISCONNECT_REQUEST, errorMessage)
            )
        }
    }
}