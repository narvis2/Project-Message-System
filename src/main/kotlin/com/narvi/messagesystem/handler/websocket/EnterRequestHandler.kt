package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.inbound.EnterRequest
import com.narvi.messagesystem.dto.websocket.outbound.EnterResponse
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.service.ChannelService
import com.narvi.messagesystem.session.WebSocketSessionManager
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class EnterRequestHandler(
    private val webSocketSessionManager: WebSocketSessionManager,
    private val channelService: ChannelService,
) : BaseRequestHandler<EnterRequest> {

    override fun handleRequest(senderSession: WebSocketSession, request: EnterRequest) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId

        val result = channelService.enter(request.channelId, senderUserId)
        val title = result.first

        if (!title.isNullOrBlank()) {
            webSocketSessionManager.sendMessage(
                senderSession,
                EnterResponse(request.channelId, title)
            )
        } else {
            webSocketSessionManager.sendMessage(
                senderSession,
                ErrorResponse(MessageType.ENTER_REQUEST, result.second.message)
            )
        }
    }
}