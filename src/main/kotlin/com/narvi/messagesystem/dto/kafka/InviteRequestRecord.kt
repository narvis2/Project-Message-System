package com.narvi.messagesystem.dto.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.InviteCode
import com.narvi.messagesystem.dto.domain.UserId

data class InviteRequestRecord @JsonCreator constructor(
    @param:JsonProperty("userId")
    val userId: UserId,
    @param:JsonProperty("userInviteCode")
    val userInviteCode: InviteCode,
) : BaseRecord(MessageType.INVITE_REQUEST)
