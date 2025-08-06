package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.inbound.FetchUserInviteCodeRequest
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.dto.websocket.outbound.FetchUserInviteCodeResponse
import com.narvi.messagesystem.service.ClientNotificationService
import com.narvi.messagesystem.service.UserService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class FetchUserInviteCodeRequestHandler(
    private val clientNotificationService: ClientNotificationService,
    private val userService: UserService,
) : BaseRequestHandler<FetchUserInviteCodeRequest> {
    override fun handleRequest(senderSession: WebSocketSession, request: FetchUserInviteCodeRequest) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId

        val inviteCode = userService.getInviteCode(senderUserId)
        if (inviteCode != null) {
            clientNotificationService.sendMessage(senderSession, senderUserId, FetchUserInviteCodeResponse(inviteCode))
        } else {
            clientNotificationService.sendMessage(
                senderSession, senderUserId, ErrorResponse(
                    MessageType.FETCH_USER_INVITECODE_REQUEST, "Fetch user invite code Failed"
                )
            )
        }
    }
}