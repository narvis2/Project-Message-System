package com.narvi.messagesystem.dto.websocket.outbound

import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.Connection

class FetchConnectionsResponse(
    val connections: List<Connection>
) : BaseMessage(MessageType.FETCH_CONNECTIONS_RESPONSE)
