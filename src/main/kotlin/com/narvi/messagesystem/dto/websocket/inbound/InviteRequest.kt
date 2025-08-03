package com.narvi.messagesystem.dto.websocket.inbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.InviteCode

data class InviteRequest @JsonCreator constructor(
    @param:JsonProperty("userInviteCode")
    val userInviteCode: InviteCode,
) : BaseRequest(MessageType.INVITE_REQUEST)
