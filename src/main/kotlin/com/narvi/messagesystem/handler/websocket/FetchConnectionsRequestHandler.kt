package com.narvi.messagesystem.handler.websocket

import com.narvi.messagesystem.constant.IdKey
import com.narvi.messagesystem.dto.domain.Connection
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.inbound.FetchConnectionsRequest
import com.narvi.messagesystem.dto.websocket.outbound.FetchConnectionsResponse
import com.narvi.messagesystem.service.ClientNotificationService
import com.narvi.messagesystem.service.UserConnectionService
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

@Component
class FetchConnectionsRequestHandler(
    private val clientNotificationService: ClientNotificationService,
    private val userConnectionService: UserConnectionService,
) : BaseRequestHandler<FetchConnectionsRequest> {
    override fun handleRequest(senderSession: WebSocketSession, request: FetchConnectionsRequest) {
        val senderUserId = senderSession.attributes[IdKey.USER_ID.value] as UserId

        val connections = userConnectionService.getUsersByStatus(senderUserId, request.status).map { user ->
            Connection(user.username, request.status)
        }

        clientNotificationService.sendMessage(senderSession, senderUserId, FetchConnectionsResponse(connections))
    }
}