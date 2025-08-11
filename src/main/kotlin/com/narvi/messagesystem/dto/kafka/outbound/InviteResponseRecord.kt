package com.narvi.messagesystem.dto.kafka.outbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.constant.UserConnectionStatus
import com.narvi.messagesystem.dto.domain.InviteCode
import com.narvi.messagesystem.dto.domain.UserId

data class InviteResponseRecord @JsonCreator constructor(
    @param:JsonProperty("userId") val userId: UserId,
    @param:JsonProperty("inviteCode") val inviteCode: InviteCode,
    @param:JsonProperty("status") val status: UserConnectionStatus,
) : BaseRecord(MessageType.INVITE_RESPONSE)