package com.narvi.messagesystem.dto.websocket.inbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType

data class RejectRequest @JsonCreator constructor(
    @param:JsonProperty("username")
    val username: String,
) : BaseRequest(MessageType.REJECT_REQUEST)
