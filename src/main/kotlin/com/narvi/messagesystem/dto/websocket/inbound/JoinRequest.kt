package com.narvi.messagesystem.dto.websocket.inbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.InviteCode

data class JoinRequest @JsonCreator constructor(
    @param:JsonProperty("inviteCode")
    val inviteCode: InviteCode,
) : BaseRequest(MessageType.JOIN_REQUEST)
