package com.narvi.messagesystem.dto.websocket.outbound

import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.ChannelId

data class JoinNotification(
    val channelId: ChannelId,
    val title: String,
) : BaseMessage(MessageType.NOTIFY_JOIN)
