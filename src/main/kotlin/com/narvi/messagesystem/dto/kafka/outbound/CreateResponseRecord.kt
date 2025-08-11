package com.narvi.messagesystem.dto.kafka.outbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.UserId

data class CreateResponseRecord @JsonCreator constructor(
    @param:JsonProperty("userId") val userId: UserId,
    @param:JsonProperty("channelId") val channelId: ChannelId,
    @param:JsonProperty("title") val title: String,
) : BaseRecord(MessageType.CREATE_RESPONSE)