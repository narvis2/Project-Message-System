package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.inbound.FetchChannelInviteCodeRequest
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.dto.websocket.outbound.FetchChannelInviteCodeResponse
import com.narvi.messagesystem.service.ChannelService
import com.narvi.messagesystem.session.WebSocketSessionManager
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class FetchChannelInviteCodeRequestHandler(
    private val webSocketSessionManager: WebSocketSessionManager,
    private val channelService: ChannelService,
) : BaseRequestHandler<FetchChannelInviteCodeRequest> {

    override fun handleRequest(senderSession: WebSocketSession, request: FetchChannelInviteCodeRequest) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId
        val channelId = request.channelId

        if (!channelService.isJoined(channelId, senderUserId)) {
            webSocketSessionManager.sendMessage(
                senderSession,
                ErrorResponse(MessageType.FETCH_CHANNEL_INVITECODE_REQUEST, "Not joined the channel.")
            )
            return
        }

        val channelInviteCode = channelService.getInviteCode(channelId)
        if (channelInviteCode != null) {
            webSocketSessionManager.sendMessage(
                senderSession, FetchChannelInviteCodeResponse(channelId, channelInviteCode)
            )
        } else {
            webSocketSessionManager.sendMessage(
                senderSession,
                ErrorResponse(MessageType.FETCH_CHANNEL_INVITECODE_REQUEST, "Fetch channel invite code failed.")
            )
        }
    }
}