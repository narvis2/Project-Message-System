package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.constant.UserConnectionStatus
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.inbound.InviteRequest
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.dto.websocket.outbound.InviteNotification
import com.narvi.messagesystem.dto.websocket.outbound.InviteResponse
import com.narvi.messagesystem.service.UserConnectionService
import com.narvi.messagesystem.session.WebSocketSessionManager
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class InviteRequestHandler(
    private val webSocketSessionManager: WebSocketSessionManager,
    private val userConnectionService: UserConnectionService,
) : BaseRequestHandler<InviteRequest> {
    override fun handleRequest(senderSession: WebSocketSession, request: InviteRequest) {
        val inviterUserId = senderSession.attributes[IdKey.USER_ID.value] as? UserId ?: return
        val (partnerUserId, inviterUsername) = userConnectionService.invite(inviterUserId, request.userInviteCode)

        if (partnerUserId != null) {
            webSocketSessionManager.sendMessage(
                senderSession,
                InviteResponse(request.userInviteCode, UserConnectionStatus.PENDING)
            )

            webSocketSessionManager.getSession(partnerUserId)?.let {
                webSocketSessionManager.sendMessage(
                    it,
                    InviteNotification(inviterUsername)
                )
            }
        } else {
            val errorMessage = inviterUsername
            webSocketSessionManager.sendMessage(senderSession, ErrorResponse(MessageType.INVITE_REQUEST, errorMessage))
        }
    }
}