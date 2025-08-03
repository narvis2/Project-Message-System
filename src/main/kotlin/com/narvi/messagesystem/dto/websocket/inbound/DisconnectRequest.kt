package com.narvi.messagesystem.dto.websocket.inbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.InviteCode

data class DisconnectRequest @JsonCreator constructor(
    @param:JsonProperty("username")
    val username: String,
) : BaseRequest(MessageType.DISCONNECT_REQUEST)
