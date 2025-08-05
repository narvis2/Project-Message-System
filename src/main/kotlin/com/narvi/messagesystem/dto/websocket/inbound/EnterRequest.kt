package com.narvi.messagesystem.dto.websocket.inbound

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.InviteCode

data class EnterRequest @JsonCreator constructor(
    @param:JsonProperty("channelId")
    val channelId: ChannelId,
) : BaseRequest(MessageType.ENTER_REQUEST)
