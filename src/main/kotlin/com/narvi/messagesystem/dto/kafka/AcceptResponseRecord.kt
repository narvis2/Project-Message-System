package com.narvi.messagesystem.dto.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.UserId

data class AcceptResponseRecord @JsonCreator constructor(
    @param:JsonProperty("userId") val userId: UserId,
    @param:JsonProperty("username") val username: String,
) : BaseRecord(MessageType.ACCEPT_RESPONSE)
