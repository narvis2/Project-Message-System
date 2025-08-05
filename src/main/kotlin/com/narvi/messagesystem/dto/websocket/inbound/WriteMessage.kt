package com.narvi.messagesystem.dto.websocket.inbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.ChannelId

data class WriteMessage @JsonCreator constructor(
    @param:JsonProperty("channelId") val channelId: ChannelId, @param:JsonProperty("content") val content: String
) : BaseRequest(MessageType.WRITE_MESSAGE)
