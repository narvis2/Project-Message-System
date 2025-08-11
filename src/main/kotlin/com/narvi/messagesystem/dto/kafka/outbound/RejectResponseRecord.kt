package com.narvi.messagesystem.dto.kafka.outbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.constant.UserConnectionStatus
import com.narvi.messagesystem.dto.domain.UserId

data class RejectResponseRecord @JsonCreator constructor(
    @param:JsonProperty("userId") val userId: UserId,
    @param:JsonProperty("username") val username: String,
    @param:JsonProperty("status") val status: UserConnectionStatus,
) : BaseRecord(MessageType.REJECT_RESPONSE)