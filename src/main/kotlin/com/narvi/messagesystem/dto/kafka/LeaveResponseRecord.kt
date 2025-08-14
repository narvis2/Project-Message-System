package com.narvi.messagesystem.dto.kafka

import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.UserId

data class LeaveResponseRecord(
    @param:JsonProperty("userId")
    val userId: UserId,
) : BaseRecord(MessageType.LEAVE_RESPONSE)