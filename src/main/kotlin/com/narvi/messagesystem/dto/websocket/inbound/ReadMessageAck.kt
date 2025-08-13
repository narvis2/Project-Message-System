package com.narvi.messagesystem.dto.websocket.inbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.MessageSeqId

data class ReadMessageAck @JsonCreator constructor(
    @param:JsonProperty("channelId") val channelId: ChannelId,
    @param:JsonProperty("messageSeqId") val messageSeqId: MessageSeqId,
) : BaseRequest(MessageType.READ_MESSAGE_ACK)
