package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.inbound.EnterRequest
import com.narvi.messagesystem.dto.websocket.outbound.EnterResponse
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.service.ChannelService
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class EnterRequestHandler(
    private val clientNotificationService: ClientNotificationService,
    private val channelService: ChannelService,
) : BaseRequestHandler<EnterRequest> {

    override fun handleRequest(senderSession: WebSocketSession, request: EnterRequest) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId

        val result = channelService.enter(request.channelId, senderUserId)
        val channelEntry = result.first

        if (channelEntry != null) {
            clientNotificationService.sendMessage(
                senderSession,
                senderUserId,
                EnterResponse(
                    channelId = request.channelId,
                    title = channelEntry.title,
                    lastReadMessageSeqId = channelEntry.lastReadMessageSeqId,
                    lastChannelMessageSeqId = channelEntry.lastChannelMessageSeqId,
                )
            )
        } else {
            clientNotificationService.sendMessage(
                senderSession, senderUserId, ErrorResponse(MessageType.ENTER_REQUEST, result.second.message)
            )
        }
    }
}