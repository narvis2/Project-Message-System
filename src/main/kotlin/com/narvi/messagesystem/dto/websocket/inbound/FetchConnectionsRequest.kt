package com.narvi.messagesystem.dto.websocket.inbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.constant.UserConnectionStatus
import com.narvi.messagesystem.dto.domain.InviteCode

data class FetchConnectionsRequest @JsonCreator constructor(
    @param:JsonProperty("status")
    val status: UserConnectionStatus,
) : BaseRequest(MessageType.FETCH_CONNECTIONS_REQUEST)
