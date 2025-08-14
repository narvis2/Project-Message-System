package com.narvi.messagesystem.dto.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.InviteCode
import com.narvi.messagesystem.dto.domain.UserId

data class JoinRequestRecord @JsonCreator constructor(
    @param:JsonProperty("userId")
    val userId: UserId,
    @param:JsonProperty("inviteCode")
    val inviteCode: InviteCode,
) : BaseRecord(MessageType.JOIN_REQUEST)
