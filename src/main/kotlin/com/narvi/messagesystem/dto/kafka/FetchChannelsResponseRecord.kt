package com.narvi.messagesystem.dto.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.Channel
import com.narvi.messagesystem.dto.domain.UserId

data class FetchChannelsResponseRecord @JsonCreator constructor(
    @param:JsonProperty("userId")
    val userId: UserId,
    @param:JsonProperty("channel")
    val channels: List<Channel>
) : BaseRecord(MessageType.FETCH_CHANNELS_RESPONSE)