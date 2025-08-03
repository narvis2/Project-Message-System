package com.narvi.messagesystem.dto.websocket.outbound

import com.narvi.messagesystem.constant.MessageType

data class MessageNotification(
    val username: String,
    val content: String,
) : BaseMessage(MessageType.NOTIFY_MESSAGE)