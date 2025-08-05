package com.narvi.messagesystem.dto.websocket.outbound

import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.ChannelId

data class CreateResponse(
    val channelId: ChannelId,
    val title: String,
) : BaseMessage(MessageType.CREATE_RESPONSE)
