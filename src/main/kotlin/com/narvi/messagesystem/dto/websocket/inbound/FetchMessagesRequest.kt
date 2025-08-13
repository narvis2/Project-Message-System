package com.narvi.messagesystem.dto.websocket.inbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.MessageSeqId

data class FetchMessagesRequest @JsonCreator constructor(
    @param:JsonProperty("channelId") val channelId: ChannelId,
    @param:JsonProperty("startMessageSeqId") val startMessageSeqId: MessageSeqId,
    @param:JsonProperty("endMessageSeqId") val endMessageSeqId: MessageSeqId
) : BaseRequest(MessageType.FETCH_MESSAGES_REQUEST)
