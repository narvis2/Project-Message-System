package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.constant.ResultType
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.inbound.JoinRequest
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.dto.websocket.outbound.JoinResponse
import com.narvi.messagesystem.service.ChannelService
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class JoinRequestHandler(
    private val clientNotificationService: ClientNotificationService,
    private val channelService: ChannelService,
) : BaseRequestHandler<JoinRequest> {

    override fun handleRequest(senderSession: WebSocketSession, request: JoinRequest) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId

        val (channel, errorType) = runCatching {
            channelService.join(request.inviteCode, senderUserId)
        }.getOrElse {
            clientNotificationService.sendMessage(
                senderSession, senderUserId, ErrorResponse(MessageType.JOIN_REQUEST, ResultType.FAILED.message)
            )
            return
        }

        val response = if (channel != null) {
            JoinResponse(channel.channelId, channel.title)
        } else {
            ErrorResponse(MessageType.JOIN_REQUEST, errorType.message)
        }

        clientNotificationService.sendMessage(senderSession, senderUserId, response)
    }
}