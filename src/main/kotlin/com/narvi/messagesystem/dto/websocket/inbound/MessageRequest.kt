package com.narvi.messagesystem.dto.websocket.inbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType

data class MessageRequest @JsonCreator constructor(
    @JsonProperty("username")
    val username: String,
    @JsonProperty("content")
    val content: String
) : BaseRequest(MessageType.MESSAGE) {

}
