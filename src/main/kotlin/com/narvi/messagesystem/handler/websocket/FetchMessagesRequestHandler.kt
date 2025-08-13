package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.constant.ResultType
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.inbound.FetchMessagesRequest
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.dto.websocket.outbound.FetchMessagesResponse
import com.narvi.messagesystem.service.ClientNotificationService
import com.narvi.messagesystem.service.MessageService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class FetchMessagesRequestHandler(
    private val messageService: MessageService,
    private val clientNotificationService: ClientNotificationService,
) : BaseRequestHandler<FetchMessagesRequest> {

    override fun handleRequest(senderSession: WebSocketSession, request: FetchMessagesRequest) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId
        val channelId = request.channelId

        val result = messageService.getMessages(
            channelId = channelId,
            startMessageSeqId = request.startMessageSeqId,
            endMessageSeqId = request.endMessageSeqId
        )

        if (result.second == ResultType.SUCCESS) {
            clientNotificationService.sendMessage(
                senderSession,
                senderUserId,
                FetchMessagesResponse(
                    channelId = channelId,
                    messages = result.first
                )
            )
        } else {
            clientNotificationService.sendMessage(
                senderSession,
                senderUserId,
                ErrorResponse(MessageType.FETCH_MESSAGES_REQUEST, result.second.message)
            )
        }
    }
}