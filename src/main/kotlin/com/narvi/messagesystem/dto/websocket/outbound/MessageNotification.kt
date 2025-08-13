package com.narvi.messagesystem.dto.websocket.outbound

import com.narvi.messagesystem.constant.MessageType
import com.narvi.messagesystem.dto.domain.ChannelId
import com.narvi.messagesystem.dto.domain.MessageSeqId

data class MessageNotification(
    val channelId: ChannelId,
    val messageSeqId: MessageSeqId,
    val username: String,
    val content: String,
) : BaseMessage(MessageType.NOTIFY_MESSAGE)