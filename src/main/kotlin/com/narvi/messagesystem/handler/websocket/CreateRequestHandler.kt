package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.constant.ResultType
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.inbound.CreateRequest
import com.narvi.messagesystem.dto.websocket.outbound.CreateResponse
import com.narvi.messagesystem.dto.websocket.outbound.ErrorResponse
import com.narvi.messagesystem.dto.websocket.outbound.JoinNotification
import com.narvi.messagesystem.service.ChannelService
import com.narvi.messagesystem.service.UserService
import com.narvi.messagesystem.session.WebSocketSessionManager
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class CreateRequestHandler(
    private val webSocketSessionManager: WebSocketSessionManager,
    private val channelService: ChannelService,
    private val userService: UserService,
) : BaseRequestHandler<CreateRequest> {

    override fun handleRequest(senderSession: WebSocketSession, request: CreateRequest) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId

        val participantId = userService.getUserId(request.participantUsername)
        if (participantId == null) {
            webSocketSessionManager.sendMessage(
                senderSession,
                ErrorResponse(MessageType.CREATE_REQUEST, ResultType.NOT_FOUND.message)
            )
            return
        }

        val result = try {
            channelService.create(senderUserId, participantId, request.title)
        } catch (e: Exception) {
            webSocketSessionManager.sendMessage(
                senderSession,
                ErrorResponse(MessageType.CREATE_REQUEST, ResultType.FAILED.message)
            )
            return
        }

        val channel = result.first
        if (channel == null) {
            webSocketSessionManager.sendMessage(
                senderSession,
                ErrorResponse(MessageType.CREATE_REQUEST, result.second.message)
            )
            return
        }

        webSocketSessionManager.sendMessage(senderSession, CreateResponse(channel.channelId, channel.title))
        webSocketSessionManager.getSession(participantId)?.let {
            webSocketSessionManager.sendMessage(
                it,
                JoinNotification(channelId = channel.channelId, title = channel.title)
            )
        }
    }
}