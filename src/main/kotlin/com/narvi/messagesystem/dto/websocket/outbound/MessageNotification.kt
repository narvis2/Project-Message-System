package com.narvi.messagesystem.dto.websocket.outbound

import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.ChannelId

data class MessageNotification(
    val channelId: ChannelId,
    val username: String,
    val content: String,
) : BaseMessage(MessageType.NOTIFY_MESSAGE)