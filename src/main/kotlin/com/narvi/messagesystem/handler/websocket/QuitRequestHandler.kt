package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.constant.ResultType
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.inbound.QuitRequest
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.dto.websocket.outbound.QuitResponse
import com.narvi.messagesystem.service.ChannelService
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class QuitRequestHandler(
    private val clientNotificationService: ClientNotificationService,
    private val channelService: ChannelService,
) : BaseRequestHandler<QuitRequest> {

    override fun handleRequest(senderSession: WebSocketSession, request: QuitRequest) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId

        val result = runCatching {
            channelService.quit(request.channelId, senderUserId)
        }.getOrElse {
            clientNotificationService.sendMessage(
                senderSession, senderUserId, ErrorResponse(MessageType.QUIT_REQUEST, ResultType.FAILED.message)
            )
            return
        }

        val response = if (result == ResultType.SUCCESS) {
            QuitResponse(request.channelId)
        } else {
            ErrorResponse(MessageType.QUIT_REQUEST, result.message)
        }

        clientNotificationService.sendMessage(senderSession, senderUserId, response)
    }
}