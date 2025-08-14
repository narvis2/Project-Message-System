package com.narvi.messagesystem.dto.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.MessageSeqId
import com.narvi.messagesystem.dto.domain.UserId
import com.narvi.messagesystem.dto.websocket.outbound.BaseMessage

data class EnterResponseRecord @JsonCreator constructor(
    @param:JsonProperty("userId")
    val userId: UserId,
    @param:JsonProperty("channelId")
    val channelId: ChannelId,
    @param:JsonProperty("title")
    val title: String,
    @param:JsonProperty("lastReadMessageSeqId")
    val lastReadMessageSeqId: MessageSeqId,
    @param:JsonProperty("lastChannelMessageSeqId")
    val lastChannelMessageSeqId: MessageSeqId,
) : BaseRecord(MessageType.ENTER_RESPONSE)
