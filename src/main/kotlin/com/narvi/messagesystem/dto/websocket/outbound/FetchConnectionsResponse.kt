package com.narvi.messagesystem.dto.websocket.outbound

import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.constant.UserConnectionStatus
import com.narvi.messagesystem.dto.domain.Connection
import com.narvi.messagesystem.dto.domain.InviteCode

data class FetchConnectionsResponse(
    val connections: List<Connection>
) : BaseMessage(MessageType.FETCH_CONNECTIONS_RESPONSE)
