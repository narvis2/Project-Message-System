package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.inbound.FetchChannelsRequest
import com.narvi.messagesystem.dto.websocket.outbound.FetchChannelsResponse
import com.narvi.messagesystem.service.ChannelService
import com.narvi.messagesystem.service.ClientNotificationService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class FetchChannelsRequestHandler(
    private val clientNotificationService: ClientNotificationService,
    private val channelService: ChannelService,
) : BaseRequestHandler<FetchChannelsRequest> {
    override fun handleRequest(senderSession: WebSocketSession, request: FetchChannelsRequest) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId

        clientNotificationService.sendMessage(
            senderSession, senderUserId, FetchChannelsResponse(channelService.getChannels(senderUserId))
        )
    }
}