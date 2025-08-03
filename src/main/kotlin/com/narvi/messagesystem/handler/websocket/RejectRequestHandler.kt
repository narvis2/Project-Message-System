package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.Constants
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.constant.UserConnectionStatus
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.inbound.RejectRequest
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.dto.websocket.outbound.RejectResponse
import com.narvi.messagesystem.service.UserConnectionService
import com.narvi.messagesystem.session.WebSocketSessionManager
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class RejectRequestHandler(
    private val userConnectionService: UserConnectionService,
    private val webSocketSessionManager: WebSocketSessionManager,
) : BaseRequestHandler<RejectRequest> {
    override fun handleRequest(senderSession: WebSocketSession, request: RejectRequest) {
        val acceptorUserId = senderSession.attributes[Constants.USER_ID.value] as UserId

        val result = userConnectionService.reject(acceptorUserId, request.username)
        val isSuccess = result.first

        if (isSuccess) {
            webSocketSessionManager.sendMessage(
                senderSession,
                RejectResponse(
                    request.username,
                    UserConnectionStatus.REJECTED
                )
            )
        } else {
            val errorMessage = result.second
            webSocketSessionManager.sendMessage(
                senderSession,
                ErrorResponse(
                    MessageType.REJECT_REQUEST,
                    errorMessage,
                )
            )
        }
    }
}