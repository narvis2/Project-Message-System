package com.narvi.messagesystem.dto.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.InviteCode
import com.narvi.messagesystem.dto.domain.UserId

data class FetchChannelInviteCodeResponseRecord @JsonCreator constructor(
    @param:JsonProperty("userId")
    val userId: UserId,
    @param:JsonProperty("channelId")
    val channelId: ChannelId,
    @param:JsonProperty("inviteCode")
    val inviteCode: InviteCode,
) : BaseRecord(MessageType.FETCH_CHANNEL_INVITECODE_RESPONSE)