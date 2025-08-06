package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.inbound.LeaveRequest
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.dto.websocket.outbound.LeaveResponse
import com.narvi.messagesystem.service.ChannelService
import com.narvi.messagesystem.session.WebSocketSessionManager
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class LeaveRequestHandler(
    private val webSocketSessionManager: WebSocketSessionManager,
    private val channelService: ChannelService,
) : BaseRequestHandler<LeaveRequest> {

    override fun handleRequest(senderSession: WebSocketSession, request: LeaveRequest) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId

        val response = if (channelService.leave(senderUserId)) {
            LeaveResponse
        } else {
            ErrorResponse(MessageType.LEAVE_REQUEST, "Leave failed.")
        }

        webSocketSessionManager.sendMessage(senderSession, response)
    }
}