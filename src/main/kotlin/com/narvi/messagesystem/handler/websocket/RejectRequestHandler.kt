package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.constant.UserConnectionStatus
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.inbound.RejectRequest
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.dto.websocket.outbound.RejectResponse
import com.narvi.messagesystem.service.ClientNotificationService
import com.narvi.messagesystem.service.UserConnectionService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class RejectRequestHandler(
    private val userConnectionService: UserConnectionService,
    private val clientNotificationService: ClientNotificationService,
) : BaseRequestHandler<RejectRequest> {
    override fun handleRequest(senderSession: WebSocketSession, request: RejectRequest) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId

        val result = userConnectionService.reject(senderUserId, request.username)
        val isSuccess = result.first

        if (isSuccess) {
            clientNotificationService.sendMessage(
                senderSession,
                senderUserId,
                RejectResponse(
                    request.username,
                    UserConnectionStatus.REJECTED
                )
            )
        } else {
            val errorMessage = result.second
            clientNotificationService.sendMessage(
                senderSession,
                senderUserId,
                ErrorResponse(
                    MessageType.REJECT_REQUEST,
                    errorMessage,
                )
            )
        }
    }
}