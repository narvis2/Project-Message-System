package com.narvi.messagesystem.dto.websocket.inbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType

data class WriteMessageRequest @JsonCreator constructor(
    @param:JsonProperty("username")
    val username: String,
    @param:JsonProperty("content")
    val content: String
) : BaseRequest(MessageType.WRITE_MESSAGE) {

}
